const Job = require("../models/jobs");
const { catchAsync, AppError } = require("@ark-industries/gogreen-common");

const createJob = catchAsync(async (req, res, next) => {
  const {
    title,
    description,
    categories,
    budget,
    expactedDuration,
    paymentType,
    attachments,
  } = req.body;

  const isAllowed = req.currentUser.userType === "talent";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const job = await Job.create({
    title,
    description,
    categories,
    budget,
    expactedDuration,
    paymentType,
    attachments,
  });

  res.status(201).json({
    status: "success",
    data: { job },
  });
});

const getJobs = catchAsync(async (req, res, next) => {
  const { limit = 10, offset = 1 } = req.params;

  const totalJobs = await Job.countDocuments();
  const totalPages = Math.ceil(totalJobs / limit);
  const skip = (offset - 1) * limit;

  const jobs = await Job.find().skip(skip).limit(limit).lean();

  res.status(200).json({
    status: "success",
    totalJobs,
    totalPages,
    offset,
    data: { jobs },
  });
});

const searchJobs = catchAsync(async (req, res, next) => {
  const {
    query = "",
    limit = 10,
    offset = 1,
    location = "",
    price = 9,
  } = req.params;

  const searchQuery = {
    $or: [{ title: query }, { description: query }],
  };

  if (location) {
    const [latitude, longitude] = location.split(",");
    const maxDistanceInKilometers = 500;

    searchQuery.location = {
      $near: {
        $geometry: {
          type: "Point",
          coordinates: [parseFloat(longitude), parseFloat(latitude)],
        },
        $maxDistance: maxDistanceInKilometers * 1000, // Convert kilometers to meters
      },
    };
  }

  if (price) {
    searchQuery.price = { $gte: parseFloat(price) };
  }

  const totalJobs = await Job.countDocuments(searchQuery);
  const totalPages = Math.ceil(totalJobs / limit);
  const skip = (offset - 1) * limit;

  const jobs = await Job.find(searchQuery).skip(skip).limit(limit).lean();

  res.status(200).json({
    status: "success",
    totalJobs,
    totalPages,
    offset,
    data: { jobs },
  });
});

const getJob = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  res.status(200).json({
    status: "success",
    data: {
      job,
    },
  });
});

const updateJob = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const {
    title,
    description,
    categories,
    budget,
    status,
    expactedDuration,
    paymentType,
  } = req.body;

  const job = await Job.findById(id);

  if (!job) {
    next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const isAllowed = req.currentUser.id === job.userId.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  job.title = title || job.title;
  job.description = description || job.description;
  job.categories = categories || job.categories;
  job.budget = budget || job.budget;
  job.status = status || job.status;
  job.expactedDuration = expactedDuration || job.expactedDuration;
  job.paymentType = paymentType || job.paymentType;

  await job.save();

  res.status(200).json({
    status: "success",
    data: {
      job,
    },
  });
});

const deleteJob = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findByIdAndDelete(id);

  if (!job) {
    next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const isAllowed =
    req.currentUser.id === job.userId.toString() ||
    req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const createJobAttachment = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 204));
  }

  const isAllowed = req.currentUser.id === job.userId.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const { attachments } = req.body;

  job.attachments.push(...attachments);

  await job.save();

  res.status(200).json({
    status: "success",
    data: {
      job,
    },
  });
});

const getJobAttachments = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 204));
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
    return next(new AppError(`No job found with matching id: ${id}`, 204));
  }

  const attachment = job.attachments.find(
    (attachment) => attachment._id.toString() === attachmentid
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
    return next(new AppError(`No job found with matching id: ${id}`, 204));
  }

  const attachmentIndex = job.attachments.findIndex(
    (attachment) => attachment._id.toString() === attachmentid
  );

  if (attachmentIndex === -1) {
    return next(
      new AppError(`No attachment found with matching id: ${attachmentid}`, 404)
    );
  }

  job.attachments.splice(attachmentIndex, 1);
  await job.save();

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
    return next(new AppError(`No job found with matching id: ${id}`, 204));
  }

  const isAllowed = req.currentUser.id === job.userId.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  job.attachments = [];
  await job.save();

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
