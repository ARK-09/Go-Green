const Proposal = require("../models/proposals");
const Job = require("../models/jobs");
const User = require("../models/user");
const {
  checkUserPermission,
  updateProposalStatus,
} = require("../utils/proposalUtil");
const {
  catchAsync,
  AppError,
  natsWrapper,
} = require("@ark-industries/gogreen-common");
const extractValidProperties = require("../utils/extractValidProperties");
const JobUpdatedPublisher = require("../events/jobUpdatedPublisher");
const ProposalCreatedPublisher = require("../events/proposalCreatedPublisher");
const ProposalUpdatedPublisher = require("../events/proposalUpdatedPublisher");
const ProposalFeedbackCreatedPublisher = require("../events/proposalFeedbackCreatedPublisher");

const fieldsToExclude = [
  "password",
  "isActive",
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

const createJobProposal = catchAsync(async (req, res, next) => {
  const jobId = req.params.id;

  const job = await Job.findById(jobId);

  if (!job) {
    return next(new AppError(`No job found with id: ${jobId}`, 404));
  }

  const { bidAmount, coverLetter, proposedDuration } = req.body;

  let { attachments } = req.body;

  attachments = extractValidProperties(attachments, [
    "id",
    "mimeType",
    "originalName",
    "createdDate",
  ]);

  if (bidAmount > job.budget) {
    return next(
      new AppError(
        "Bid amount should be less then or equal to the job budget.",
        400
      )
    );
  }

  const isAllowed = req.currentUser.userType === "talent";

  if (!isAllowed) {
    return next(new AppError("You'r not allowed to perform this action.", 403));
  }

  const isAlreadyApplied = await Proposal.findOne({
    type: "Jobs",
    doc: jobId,
    user: req.currentUser._id.toString(),
  });

  if (isAlreadyApplied) {
    return next(new AppError("Already applied.", 409));
  }

  if (job.status === "Completed") {
    return next(new AppError("The job is closed.", 410));
  }

  const proposalObject = {
    doc: jobId,
    bidAmount,
    coverLetter,
    proposedDuration,
    type: "Jobs",
    user: req.currentUser._id.toString(),
  };

  if (attachments) {
    proposalObject.attachments = attachments;
  }

  const proposal = await Proposal.create(proposalObject);

  await new ProposalCreatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const proposalPopulated = await proposal.populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  res.status(200).json({
    status: "success",
    data: {
      proposal: proposalPopulated,
    },
  });
});

const createServiceProposal = catchAsync(async (req, res, next) => {
  const serviceId = req.params.id;

  const service = await Job.findById(serviceId);

  if (!service) {
    return next(new AppError(`No service found with id: ${serviceId}`, 404));
  }

  const isAllowed = req.currentUser.userType === "client";

  if (!isAllowed) {
    return next(new AppError("You'r not allowed to perform this action.", 403));
  }

  const isAlreadyApplied = await Proposal.findOne({
    user: req.currentUser._id.toString(),
  });

  if (isAlreadyApplied) {
    return next(new AppError("Already applied.", 409));
  }

  const { bidAmount, coverLetter, proposedDuration } = req.body;

  let { attachments } = req.body;

  attachments = extractValidProperties(attachments, [
    "id",
    "mimeType",
    "originalName",
    "createdDate",
  ]);

  if (bidAmount > service.budget) {
    return next(
      new AppError(
        "Bid amount should be less then or equal to the service budget.",
        400
      )
    );
  }

  const serviceObject = {
    doc: serviceId,
    bidAmount,
    coverLetter,
    proposedDuration,
    type: "Services",
    user: req.currentUser._id.toString(),
  };

  if (attachments) {
    serviceObject.attachments = attachments;
  }

  const proposal = await Proposal.create(serviceObject);

  await new ProposalCreatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const proposalPopulated = await proposal.populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  res.status(200).json({
    status: "success",
    data: {
      proposal: proposalPopulated,
    },
  });
});

const isAlreadyApplied = catchAsync(async (req, res, next) => {
  const { jobid } = req.params;

  const job = await Job.findById(jobid);

  if (!job) {
    return next(new AppError(`No job found with id: ${jobid}`, 404));
  }

  const proposal = await Proposal.findOne({
    user: req.currentUser._id.toString(),
    type: "Jobs",
    doc: job._id.toString(),
  });

  if (!proposal) {
    return res.status(200).json({
      status: "success",
      data: {
        alreadyApplied: false,
      },
    });
  }

  res.status(200).json({
    status: "success",
    data: {
      alreadyApplied: true,
      status: proposal.status,
    },
  });
});

const createInterview = catchAsync(async (req, res, next) => {
  const proposalId = req.params.id;

  const proposal = await Proposal.findById(proposalId);

  if (!proposal) {
    return next(new AppError(`No proposal found with id: ${proposalId}`, 404));
  }

  const proposalType = proposal.type;

  if (proposalType === "Services") {
    const job = await Job.findById(proposal.doc);

    const isAllowed = req.currentUser._id.toString() === job.user.toString();

    if (!isAllowed) {
      return next(
        new AppError("You'r not allowed to perform this action.", 403)
      );
    }

    job.status = "In Review";
    await job.save();
    await new JobUpdatedPublisher(natsWrapper.client).publish(job).catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });
  } else {
    const job = await Job.findById(proposal.doc);

    const isAllowed = req.currentUser._id.toString() === job.user.toString();

    if (!isAllowed) {
      return next(
        new AppError("You'r not allowed to perform this action.", 403)
      );
    }

    job.status = "In Review";
    await job.save();
    await new JobUpdatedPublisher(natsWrapper.client).publish(job).catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });
  }

  const isAlreadyInterviewing = proposal.status === "Accepted";

  if (isAlreadyInterviewing) {
    return next(new AppError("Already interviewing the user.", 409));
  }

  proposal.status = "Accepted";
  await proposal.save();

  await new ProposalUpdatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: {
      message: "Successfully initiated the interview process with the user.",
    },
  });
});

const hireProposal = catchAsync(async (req, res, next) => {
  const { jobid, id } = req.params;

  const job = await Job.findById(jobid);

  if (!job) {
    return next(new AppError(`No job found with id: ${jobid}`, 404));
  }

  if (job.status === "Completed") {
    return next(new AppError("The job is not open.", 403));
  }

  const proposal = await Proposal.findOne({ _id: id, type: "Jobs" });

  if (!proposal) {
    return next(new AppError(`No proposal is found with id: ${id}`, 404));
  }

  if (proposal.status === "Hired") {
    return next(new AppError("Already hired!.", 409));
  }

  if (proposal.status === "Declined" || proposal.status === "Withdraw") {
    return next(
      new AppError("Can't hire a proposal who is withdrawn or declined.", 403)
    );
  }

  const isAllowed = job.user.toString() === req.currentUser._id.toString();

  if (!isAllowed) {
    return next(new AppError("You'r not allowed to perform this action.", 403));
  }

  const talentCount = await Proposal.countDocuments({
    doc: jobid,
    type: "Jobs",
    status: "Hired",
  });

  if (talentCount >= job.talentCount) {
    return next(
      new AppError(
        `Hiring talents exceeding the required amount which is: ${job.talentCount}.`,
        422
      )
    );
  }

  job.status = "Assigned";
  proposal.status = "Hired";
  await job.save();
  await proposal.save();

  await new JobUpdatedPublisher(natsWrapper.client).publish(job).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });
  await new ProposalUpdatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: {
      message: "Hired successfully",
    },
  });
});

const getProposals = catchAsync(async (req, res, next) => {
  const proposals = await Proposal.find({
    user: req.currentUser._id.toString(),
  })
    .sort({ createdDate: 1 })
    .populate("user", `-${fieldsToExclude.join(" -")}`);

  const proposalsWithJob = await Promise.all(
    proposals.map(async (proposal) => {
      const proposalType = proposal.type;

      let proposalWithJob = proposal.toObject();

      if (proposalType === "Services") {
        const job = await Job.findById(proposal.doc).populate([
          { path: "user", select: `-${fieldsToExclude.join(" -")}` },
          { path: "categories", select: "-__v" },
          { path: "skills", select: "-__v" },
        ]);
        proposalWithJob.job = job;
        return proposalWithJob;
      } else if (proposalType === "Jobs") {
        const job = await Job.findById(proposal.doc).populate([
          { path: "user", select: `-${fieldsToExclude.join(" -")}` },
          { path: "categories", select: "-__v" },
          { path: "skills", select: "-__v" },
        ]);
        proposalWithJob.job = job;
        return proposalWithJob;
      }

      return proposal;
    })
  );

  return res.status(200).json({
    status: "success",
    data: {
      proposals: proposalsWithJob,
    },
  });
});

const getJobProposals = catchAsync(async (req, res, next) => {
  const jobId = req.params.id;

  const job = await Job.findById(jobId);

  if (!job) {
    return next(new AppError(`No job found with id: ${jobId}`, 404));
  }

  const isJobOwner = req.currentUser._id.toString() === job.user.toString();

  let proposals = null;

  if (isJobOwner) {
    proposals = await Proposal.find({
      type: "Jobs",
      doc: jobId,
    })
      .sort({ createdDate: 1 })
      .populate("user", `-${fieldsToExclude.join(" -")}`);
  } else {
    proposals = await Proposal.find({
      type: "Jobs",
      doc: jobId,
      user: req.currentUser._id.toString(),
    })
      .sort({ createdDate: 1 })
      .populate("user", `-${fieldsToExclude.join(" -")}`);
  }

  const proposalsWithJob = await Promise.all(
    proposals.map(async (proposal) => {
      let proposalWithJob = proposal.toObject();

      const job = await Job.findById(proposal.doc).populate([
        { path: "user", select: `-${fieldsToExclude.join(" -")}` },
        { path: "categories", select: "-__v" },
        { path: "skills", select: "-__v" },
      ]);
      proposalWithJob.job = job;
      return proposalWithJob;
    })
  );

  res.status(200).json({
    status: "success",
    data: {
      proposals: proposalsWithJob,
    },
  });
});

const getServiceProposals = catchAsync(async (req, res, next) => {
  const serviceId = req.params.id;

  const service = await Job.findById(serviceId);

  if (!service) {
    return next(new AppError(`No service found with id: ${serviceId}`, 404));
  }

  const isAllowed = req.currentUser._id.toString() === service.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this operation.", 403)
    );
  }

  const proposals = await Proposal.find({
    type: "Services",
    doc: serviceId,
    user: req.currentUser._id.toString(),
  })
    .sort({ createdDate: 1 })
    .populate("user", `-${fieldsToExclude.join(" -")}`);

  const proposalsWithService = await Promise.all(
    proposals.map(async (proposal) => {
      let proposalWithJob = proposal.toObject();

      const job = await Job.findById(proposal.doc).populate([
        { path: "user", select: `-${fieldsToExclude.join(" -")}` },
        { path: "categories", select: "-__v" },
        { path: "skills", select: "-__v" },
      ]);
      proposalWithJob.job = job;
    })
  );

  res.status(200).json({
    status: "success",
    data: {
      proposals: proposalsWithService,
    },
  });
});

const getProposal = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const proposal = await Proposal.findById(id).populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  if (!proposal) {
    return next(new AppError(`No proposal found with id: ${id}`, 404));
  }

  let isAllowed = await checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    return next(
      new AppError("Your are not allowed to perform this action.", 403)
    );
  }

  let proposalWithJob = proposal.toObject();

  const job = await Job.findById(proposal.doc).populate([
    { path: "user", select: `-${fieldsToExclude.join(" -")}` },
    { path: "categories", select: "-__v" },
    { path: "skills", select: "-__v" },
  ]);

  proposalWithJob.job = job;

  res.status(200).json({
    status: "success",
    data: {
      proposal: proposalWithJob,
    },
  });
});

const deleteProposal = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const proposal = await Proposal.findById(id);

  if (!proposal) {
    return next(new AppError(`No proposal found with id: ${id}`, 404));
  }

  let isAllowed = await checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    return next(
      new AppError("Your are not allowed to perform this action.", 403)
    );
  }

  const isProposalOwner =
    req.currentUser._id.toString() === proposal.user.toString();

  if (isProposalOwner) {
    await updateProposalStatus(proposal, "Withdraw");
  } else {
    await updateProposalStatus(proposal, "Declined");
  }

  await new ProposalUpdatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: {
      proposal,
    },
  });
});

const createProposalAttachment = catchAsync(async (req, res, next) => {
  const proposalId = req.params.id;

  const proposal = await Proposal.findById(proposalId);

  if (!proposal) {
    return next(
      new AppError(`No proposal found with matching id: ${proposalId}`, 404)
    );
  }

  if (proposal.status === "Withdraw" || proposal.status === "Declined") {
    return next(
      new AppError(
        "Cant't add attachment to a proposal that has been withdrawn or declined.",
        422
      )
    );
  }

  const isAllowed = req.currentUser._id.toString() === proposal.user.toString();

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
    proposal.attachments.push(...attachments);
  } else if (typeof attachments === "object") {
    proposal.attachments.push(attachments);
  }

  await proposal.save();

  await new ProposalUpdatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const proposalPopulated = await proposal.populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  res.status(200).json({
    status: "success",
    data: {
      proposal: proposalPopulated,
    },
  });
});

const getProposalAttachments = catchAsync(async (req, res, next) => {
  const proposalId = req.params.id;

  const proposal = await Proposal.findById(proposalId);

  if (!proposal) {
    return next(
      new AppError(`No proposal found with matching id: ${proposalId}`, 404)
    );
  }

  const isAllowed = await checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    return next(
      new AppError("You dont have permission to perform this action.", 403)
    );
  }

  res.status(200).json({
    status: "success",
    data: {
      attachments: proposal.attachments,
    },
  });
});

const getProposalAttachment = catchAsync(async (req, res, next) => {
  const { id, attachmentid } = req.params;

  const proposal = await Proposal.findById(id);

  if (!proposal) {
    return next(new AppError(`No proposal found with matching id: ${id}`, 404));
  }

  const isAllowed = await checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    return next(
      new AppError("Your are not allowed to perform this action.", 403)
    );
  }

  const attachment = proposal.attachments.find(
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

const deleteProposalAttachment = catchAsync(async (req, res, next) => {
  const { id, attachmentid } = req.params;

  const proposal = await Proposal.findById(id);

  if (!proposal) {
    return next(new AppError(`No proposal found with matching id: ${id}`, 404));
  }

  const isAllowed = req.currentUser._id.toString() === proposal.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You dont have permission to perform this action.", 403)
    );
  }

  const attachmentIndex = proposal.attachments.findIndex(
    (attachment) => attachment.id.toString() === attachmentid
  );

  if (attachmentIndex === -1) {
    return next(
      new AppError(`No attachment found with matching id: ${attachmentid}`, 404)
    );
  }

  proposal.attachments.splice(attachmentIndex, 1);
  await proposal.save();

  await new ProposalUpdatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(204).json({
    status: "success",
    data: {
      attachments: proposal.attachments,
    },
  });
});

const deleteProposalAttachments = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const proposal = await Proposal.findById(id);

  if (!proposal) {
    return next(new AppError(`No proposal found with matching id: ${id}`, 404));
  }

  const isAllowed = req.currentUser._id.toString() === proposal.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  proposal.attachments = [];
  await proposal.save();

  await new ProposalUpdatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const createJobProposalFeedback = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const proposal = await Proposal.findOne({ _id: id, type: "Jobs" });

  if (!proposal) {
    return next(new AppError(`No proposal found with matching id: ${id}`, 404));
  }

  if (proposal.status !== "Hired") {
    const errorMessage =
      proposal.user.toString() === req.currentUser._id.toString()
        ? "Feedback can only be submitted if you're hired for this job."
        : "Feedback can only be submitted for hired talents.";

    return next(new AppError(errorMessage, 403));
  }

  const job = await Job.findById(proposal.doc);

  if (!job) {
    return next(
      new AppError("The proposal does not have any associated job.", 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === job.user.toString() ||
    req.currentUser._id.toString() === proposal.user.toString();

  if (!isAllowed) {
    return next(new AppError("Your not allowed to perform this action", 403));
  }

  if (job.status !== "Completed") {
    return next(
      new AppError(
        "Feedback submission is only allowed for successfully completed jobs.",
        403
      )
    );
  }

  const { rating, feedback } = req.body;

  if (req.currentUser._id.toString() === job.user.toString()) {
    proposal.clientRating = rating;
    proposal.clientFeedback = feedback;
    await proposal.save();
  } else {
    proposal.talentRating = rating;
    proposal.talentFeedback = feedback;
    await proposal.save();
  }

  await new ProposalFeedbackCreatedPublisher(natsWrapper.client)
    .publish(proposal)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  return res.status(200).json({
    status: "success",
    data: {
      proposal,
    },
  });
});

const getReviews = catchAsync(async (req, res, next) => {
  const { userid } = req.params;

  // const user = await User.findById (userid);

  // if (user.userType === "client") {
  //
  // } else if (user.userType === "talent") {

  // }

  const searchQuery = {
    user: userid,
    status: "Hired",
    clientFeedback: { $ne: null },
    talentFeedback: { $ne: null },
  };

  const noOfReviews = await Proposal.countDocuments(searchQuery);
  const proposals = await Proposal.find(searchQuery)
    .populate([
      {
        path: "doc",
        select: "+title +description +budget +location +createdDate +type",
      },
      { path: "doc.user", select: "+name +email +image" },
    ])
    .sort({ createdDate: 1 });

  const reviews = proposals.map((proposal) => {
    return {
      doc: proposal.doc,
      clientFeedback: proposal.clientFeedback,
      clientRating: proposal.clientRating,
    };
  });

  res.status(200).json({
    status: "success",
    noOfReviews,
    data: {
      reviews,
    },
  });
});

exports.createJobProposal = createJobProposal;
exports.createServiceProposal = createServiceProposal;
exports.createInterview = createInterview;
exports.isAlreadyApplied = isAlreadyApplied;
exports.hireProposal = hireProposal;
exports.getProposals = getProposals;
exports.getJobProposals = getJobProposals;
exports.getServiceProposals = getServiceProposals;
exports.getProposal = getProposal;
exports.deleteProposal = deleteProposal;
exports.createProposalAttachment = createProposalAttachment;
exports.getProposalAttachment = getProposalAttachment;
exports.getProposalAttachments = getProposalAttachments;
exports.deleteProposalAttachment = deleteProposalAttachment;
exports.deleteProposalAttachments = deleteProposalAttachments;
exports.createJobProposalFeedback = createJobProposalFeedback;
exports.getReviews = getReviews;
