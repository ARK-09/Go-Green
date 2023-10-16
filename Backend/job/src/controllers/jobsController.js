const Job = require("../models/jobs");
const Skill = require("../models/skills");
const Category = require("../models/categories");
const extractValidProperties = require("../utils/extractValidProperties");
const {
  catchAsync,
  AppError,
  natsWrapper,
} = require("@ark-industries/gogreen-common");
const JobCreatedPublisher = require("../events/jobCreatedPublisher");
const JobUpdatedPublisher = require("../events/jobUpdatedPublisher");
const JobDeletedPublisher = require("../events/jobDeletedPublisher");
const Proposal = require("../models/proposals");

const fieldsToExclude = [
  "password",
  "isActive",
  "userType",
  "invalidLoginCount",
  "phoneNo",
  "financeAllowed",
  "passwordChangedAt",
  "resetToken",
  "resetTokenExpireAt",
  "otp",
  "otpExpireAt",
  "blocked",
  "__v",
];

const createJob = catchAsync(async (req, res, next) => {
  const {
    title,
    description,
    budget,
    expectedDuration,
    paymentType,
    location,
    talentCount,
  } = req.body;

  let { attachments } = req.body;

  attachments = extractValidProperties(attachments, [
    "id",
    "mimeType",
    "originalName",
    "createdDate",
  ]);

  let { skills, categories } = req.body;

  const isAllowed = req.currentUser.userType === "client";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  skills = Array.from(new Set(skills));
  categories = Array.from(new Set(categories));

  const validSkills = await Skill.find({ _id: { $in: skills } });
  const validCategories = await Category.find({ _id: { $in: categories } });

  const invalidSkills = skills.filter((skillId) => {
    return !validSkills.some((skill) => {
      return skill._id.toString() === skillId;
    });
  });

  const invalidCategories = categories.filter((categoryId) => {
    return !validCategories.some((category) => {
      return category._id.toString() === categoryId;
    });
  });

  if (validSkills.length == 0 || invalidSkills.length > 0) {
    return next(
      new AppError(
        `The following skills not exist: ${
          validSkills.length == 0 ? skills.join(", ") : invalidSkills.join(", ")
        }`,
        404
      )
    );
  }

  if (validCategories.length == 0 || invalidCategories.length > 0) {
    return next(
      new AppError(
        `The following categories not exist: ${
          validCategories.length == 0
            ? categories.join(", ")
            : invalidCategories.join(", ")
        }`,
        404
      )
    );
  }

  const jobObject = {
    title,
    description,
    skills: skills,
    categories,
    budget,
    expectedDuration,
    paymentType,
    talentCount,
    user: req.currentUser._id.toString(),
  };

  if (attachments) {
    jobObject.attachments = attachments;
  }

  console.log(location.coordinates);

  if (location) {
    jobObject.location = {
      coordinates: [location.coordinates[0], location.coordinates[1]],
    };
  }

  const job = await Job.create(jobObject);

  await new JobCreatedPublisher(natsWrapper.client).publish(job).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });

  const jobPopulated = await job.populate([
    { path: "user", select: `-${fieldsToExclude.join(" -")}` },
    { path: "categories", select: "-__v" },
    { path: "skills", select: "-__v" },
  ]);

  res.status(201).json({
    status: "success",
    data: { job: jobPopulated },
  });
});

const getJobs = catchAsync(async (req, res, next) => {
  const { limit = 10, offset = 1 } = req.params;

  const totalJobs = await Job.countDocuments({
    user: req.currentUser._id.toString(),
  });
  const totalPages = Math.ceil(totalJobs / limit);
  const skip = (offset - 1) * limit;

  const jobs = await Job.find({ user: req.currentUser._id.toString() })
    .sort({ createdDate: 1 })
    .populate([
      { path: "user", select: `-${fieldsToExclude.join(" -")}` },
      { path: "categories", select: "-__v" },
      { path: "skills", select: "-__v" },
    ])
    .skip(skip)
    .limit(limit)
    .lean();

  const jobsResult = await Promise.all(
    jobs.map(async (job) => {
      const noOfProposalsSubmitted = await Proposal.countDocuments({
        doc: job._id,
      });

      const interviewing = await Proposal.countDocuments({
        doc: job._id,
        status: "Accepted",
      });

      const jobObject = job;
      jobObject.noOfProposals = noOfProposalsSubmitted;
      jobObject.interviewing = interviewing;

      return jobObject;
    })
  );

  res.status(200).json({
    status: "success",
    totalJobs,
    totalPages,
    offset,
    data: { jobs: jobsResult },
  });
});

const searchJobs = catchAsync(async (req, res, next) => {
  const { query = "", limit = 10, offset = 1, location, price = 9 } = req.query;

  const searchQuery = {};

  if (query !== "") {
    searchQuery.$or = [
      { title: { $regex: query, $options: "i" } },
      { description: { $regex: query, $options: "i" } },
    ];
  }

  if (price) {
    searchQuery.budget = { $gte: parseFloat(price) };
  }

  // if (req.query.location && req.query.location.coordinates) {
  //   const latitude = req.query.location.coordinates[0];
  //   const longitude = req.query.location.coordinates[1];

  //   const maxDistanceInKilometers = 500;

  //   searchQuery.location = {
  //     $near: {
  //       $geometry: {
  //         type: "Point",
  //         coordinates: [parseFloat(longitude), parseFloat(latitude)],
  //       },
  //       $maxDistance: maxDistanceInKilometers * 1000, // Convert kilometers to meters
  //     },
  //   };
  // }

  const totalJobs = await Job.countDocuments(searchQuery);
  const totalPages = Math.ceil(totalJobs / limit);
  const skip = (offset - 1) * limit;

  const jobs = await Job.find(searchQuery)
    .populate([
      { path: "user", select: `-${fieldsToExclude.join(" -")}` },
      { path: "categories", select: "-__v" },
      { path: "skills", select: "-__v" },
    ])
    .sort({ createdDate: -1 })
    .skip(skip)
    .limit(limit)
    .lean();

  const jobsResult = await Promise.all(
    jobs.map(async (job) => {
      const noOfProposalsSubmitted = await Proposal.countDocuments({
        doc: job._id,
      });

      const interviewing = await Proposal.countDocuments({
        doc: job._id,
        status: "Accepted",
      });

      const jobObject = job;
      jobObject.noOfProposals = noOfProposalsSubmitted;
      jobObject.interviewing = interviewing;

      return jobObject;
    })
  );

  res.status(200).json({
    status: "success",
    totalJobs,
    totalPages,
    offset,
    data: { jobs: jobsResult },
  });
});

const getJob = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id).populate([
    { path: "user", select: `-${fieldsToExclude.join(" -")}` },
    { path: "categories", select: "-__v" },
    { path: "skills", select: "-__v" },
  ]);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const noOfProposalsSubmitted = await Proposal.countDocuments({ doc: id });
  const interviewing = await Proposal.countDocuments({
    doc: job._id,
    status: "Accepted",
  });

  const jobResult = job.toObject();
  jobResult.noOfProposals = noOfProposalsSubmitted;
  jobResult.interviewing = interviewing;

  res.status(200).json({
    status: "success",
    data: {
      job: jobResult,
    },
  });
});

const updateJob = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const {
    title,
    description,
    budget,
    status,
    expectedDuration,
    paymentType,
    location,
  } = req.body;

  let { skills, categories } = req.body;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const isAllowed = req.currentUser._id.toString() === job.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  skills = Array.from(new Set(skills));
  categories = Array.from(new Set(categories));

  const validSkills = await Skill.find({ _id: { $in: skills } });
  const validCategories = await Category.find({ _id: { $in: categories } });

  const invalidSkills = skills.filter((skillId) => {
    return !validSkills.some((skill) => {
      return skill._id.toString() === skillId;
    });
  });

  const invalidCategories = categories.filter((categoryId) => {
    return !validCategories.some((category) => {
      return category._id.toString() === categoryId;
    });
  });

  if (
    (skills.length > 0 && validSkills.length == 0) ||
    invalidSkills.length > 0
  ) {
    return next(
      new AppError(
        `The following skills not exist: ${
          validSkills.length == 0 ? skills.join(", ") : invalidSkills.join(", ")
        }`,
        404
      )
    );
  }

  if (
    (categories.length > 0 && validCategories.length == 0) ||
    invalidCategories.length > 0
  ) {
    return next(
      new AppError(
        `The following categories not exist: ${
          validCategories.length == 0
            ? categories.join(", ")
            : invalidCategories.join(", ")
        }`,
        404
      )
    );
  }

  const skillsParsent = skills.filter((skill) => job.skills.includes(skill));

  const categoryParsent = categories.filter((category) =>
    job.categories.includes(category)
  );

  if (skillsParsent.length > 0) {
    return next(
      new AppError(`The job has already these skills: ${skillsParsent}`, 401)
    );
  }

  if (categoryParsent.length > 0) {
    return next(
      new AppError(
        `The job has already these category: ${categoryParsent}`,
        401
      )
    );
  }

  if (location) {
    job.location = {
      coordinates: [location.coordinates[0], location.coordinates[1]],
    };
  }

  job.title = title || job.title;
  job.description = description || job.description;
  job.skills = skills.length > 0 ? skills : job.skills;
  job.categories = categories.length > 0 ? categories : job.categories;
  job.budget = budget || job.budget;
  job.status = status || job.status;
  job.expectedDuration = expectedDuration || job.expectedDuration;
  job.paymentType = paymentType || job.paymentType;

  await job.save();

  await new JobUpdatedPublisher(natsWrapper.client).publish(job).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });

  const jobPopulated = await job.populate([
    { path: "user", select: `-${fieldsToExclude.join(" -")}` },
    { path: "categories", select: "-__v" },
    { path: "skills", select: "-__v" },
  ]);

  res.status(200).json({
    status: "success",
    data: {
      job: jobPopulated,
    },
  });
});

const deleteJob = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const isAllowed =
    req.currentUser._id.toString() === job.user.toString() ||
    req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  await Job.findByIdAndDelete(id);

  await new JobDeletedPublisher(natsWrapper.client)
    .publish({ id: job._id })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const createJobAttachment = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const isAllowed = req.currentUser._id.toString() === job.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  let { attachments } = req.body;

  attachments = extractValidProperties(attachments, [
    "id",
    "mimeType",
    "originalName",
    "createdDate",
  ]);

  if (attachments && Array.isArray(attachments)) {
    job.attachments.push(...attachments);
  } else if (typeof attachments === "object") {
    job.attachments.push(attachments);
  }

  await job.save();

  await new JobUpdatedPublisher(natsWrapper.client).publish(job).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });

  const jobPopulated = await job.populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  res.status(200).json({
    status: "success",
    data: {
      job: jobPopulated,
    },
  });
});

const getJobAttachments = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  res.status(200).json({
    status: "success",
    data: {
      attachments: job.attachments,
    },
  });
});

const getJobAttachment = catchAsync(async (req, res, next) => {
  const { id, attachmentid } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const attachment = job.attachments.find(
    (attachment) => attachment.id.toString() === attachmentid
  );

  if (!attachment) {
    return next(
      new AppError(`No attachment found with matching id: ${attachmentid}`, 404)
    );
  }

  res.status(200).json({
    status: "success",
    data: {
      attachment,
    },
  });
});

const deleteJobAttachment = catchAsync(async (req, res, next) => {
  const { id, attachmentid } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const isAllowed = req.currentUser._id.toString() === job.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const attachmentIndex = job.attachments.findIndex(
    (attachment) => attachment.id.toString() === attachmentid
  );

  if (attachmentIndex === -1) {
    return next(
      new AppError(`No attachment found with matching id: ${attachmentid}`, 404)
    );
  }

  job.attachments.splice(attachmentIndex, 1);
  await job.save();

  await new JobUpdatedPublisher(natsWrapper.client).publish(job).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });

  res.status(204).json({
    status: "success",
    data: {
      attachments: job.attachments,
    },
  });
});

const deleteJobAttachments = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const isAllowed = req.currentUser._id.toString() === job.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  job.attachments = [];
  await job.save();

  await new JobUpdatedPublisher(natsWrapper.client).publish(job).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });

  res.status(204).json({
    status: "success",
    data: null,
  });
});

exports.createJob = createJob;
exports.getJobs = getJobs;
exports.searchJobs = searchJobs;
exports.getJob = getJob;
exports.updateJob = updateJob;
exports.deleteJob = deleteJob;
exports.createJobAttachment = createJobAttachment;
exports.getJobAttachments = getJobAttachments;
exports.getJobAttachment = getJobAttachment;
exports.deleteJobAttachments = deleteJobAttachments;
exports.deleteJobAttachment = deleteJobAttachment;
