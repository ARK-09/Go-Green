const Proposal = require("../models/proposals");
const Job = require("../models/jobs");
const {
  checkUserPermission,
  updateProposalStatus,
} = require("../utils/proposalUtil");
const {
  catchAsync,
  AppError,
  natsWrapper,
} = require("@ark-industries/gogreen-common");
const ProposalCreatedPublisher = require("../events/proposalCreatedPublisher");
const ProposalUpdatedPublisher = require("../events/proposalUpdatedPublisher");
const ProposalDeletedPublisher = require("../events/proposalDeletedPublisher");

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

  const { bidAmount, coverLetter, proposedDuration, attachments } = req.body;

  if (bidAmount > job.budget) {
    return next(
      new AppError(
        "Bid amount should be less then or equal to the job budget.",
        400
      )
    );
  }

  const isAllowed =
    req.currentUser.userType === "talent" &&
    job.user.toString() != req.currentUser.id;

  if (!isAllowed) {
    return next(new AppError("You'r not allowed to perform this action.", 403));
  }

  const isAlreadyApplied = await Proposal.findOne({
    user: req.currentUser.id,
  });

  if (isAlreadyApplied) {
    return next(new AppError("Already applied.", 409));
  }

  if (job.status != "Open") {
    return next(new AppError("The job is closed.", 410));
  }

  const proposal = await Proposal.create({
    refId: jobId,
    bidAmount,
    coverLetter,
    proposedDuration,
    attachments,
    type: "job",
    user: req.currentUser.id,
  });

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

  const isAllowed =
    req.currentUser.userType === "client" &&
    service.user.toString() != req.currentUser.id;

  if (!isAllowed) {
    return next(new AppError("You'r not allowed to perform this action.", 403));
  }

  const isAlreadyApplied = await Proposal.findOne({
    user: req.currentUser.id,
  });

  if (isAlreadyApplied) {
    return next(new AppError("Already applied.", 409));
  }

  const { bidAmount, coverLetter, proposedDuration, attachments } = req.body;

  if (bidAmount > service.budget) {
    return next(
      new AppError(
        "Bid amount should be less then or equal to the service budget.",
        400
      )
    );
  }

  const proposal = await Proposal.create({
    refId: serviceId,
    bidAmount,
    coverLetter,
    proposedDuration,
    attachments,
    type: "service",
    user: req.currentUser.id,
  });

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

const getJobProposals = catchAsync(async (req, res, next) => {
  const jobId = req.params.id;

  const job = await Job.findById(jobId);

  if (!job) {
    return next(new AppError(`No job found with id: ${jobId}`, 404));
  }

  const isJobOwner = req.currentUser.id === job.user.toString();

  if (isJobOwner) {
    const proposals = await Proposal.find({
      $and: [{ type: "job" }, { refId: jobId }],
    }).populate("user", `-${fieldsToExclude.join(" -")}`);

    return res.status(200).json({
      status: "success",
      data: {
        proposals,
      },
    });
  }

  const proposal = await Proposal.find({
    type: "job",
    refId: jobId,
    user: req.currentUser.id,
  }).populate("user", `-${fieldsToExclude.join(" -")}`);

  res.status(200).json({
    status: "success",
    data: {
      proposal,
    },
  });
});

const getServiceProposals = catchAsync(async (req, res, next) => {
  const serviceId = req.params.id;

  const service = await Job.findById(serviceId);

  if (!service) {
    return next(new AppError(`No service found with id: ${serviceId}`, 404));
  }

  const isAllowed = req.currentUser.id === service.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this operation.", 403)
    );
  }

  const proposals = await Proposal.find({
    $and: [{ type: "service" }, { refId: serviceId }],
  }).populate("user", `-${fieldsToExclude.join(" -")}`);

  res.status(200).json({
    status: "success",
    data: {
      proposals,
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

  res.status(200).json({
    status: "success",
    data: {
      proposal,
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

  const isProposalOwner = req.currentUser.id === proposal.user.toString();

  if (isProposalOwner) {
    await updateProposalStatus(proposal, "Withdraw");
  } else {
    await updateProposalStatus(proposal, "Declined");
  }

  await new ProposalDeletedPublisher(natsWrapper.client)
    .publish({ id: proposal._id })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(204).json({
    status: "success",
    data: null,
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
        "Cant't add attachment to a proposal that has been withdrawn or declined."
      )
    );
  }

  const isAllowed = req.currentUser.id === proposal.user.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const { attachments } = req.body;

  proposal.attachments.push(...attachments);

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

  if (proposal.status === "Withdraw" || proposal.status === "Declined") {
    return next(
      new AppError(
        "Cant't add attachment to a proposal that has been withdrawn or declined."
      )
    );
  }

  const isAllowed = req.currentUser.id === proposal.user.toString();

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

  if (proposal.status === "Withdraw" || proposal.status === "Declined") {
    return next(
      new AppError(
        "Cant't add attachment to a proposal that has been withdrawn or declined."
      )
    );
  }

  const isAllowed = req.currentUser.id === proposal.user.toString();

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

exports.createJobProposal = createJobProposal;
exports.createServiceProposal = createServiceProposal;
exports.getJobProposals = getJobProposals;
exports.getServiceProposals = getServiceProposals;
exports.getProposal = getProposal;
exports.deleteProposal = deleteProposal;
exports.createProposalAttachment = createProposalAttachment;
exports.getProposalAttachment = getProposalAttachment;
exports.getProposalAttachments = getProposalAttachments;
exports.deleteProposalAttachment = deleteProposalAttachment;
exports.deleteProposalAttachments = deleteProposalAttachments;
