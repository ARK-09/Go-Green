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

const createJobProposal = catchAsync(async (req, res, next) => {
  const jobId = req.params.id;

  const job = await Job.findById(jobId);

  if (!job) {
    next(new AppError(`No job found with id: ${jobId}`, 204));
  }

  const { bidAmmount, coverLetter, proposedDuration, attachments } = req.body;

  if (bidAmmount > job.budget) {
    next(
      new AppError(
        "Bid amount should be less then or equal to the job budget.",
        400
      )
    );
  }

  const proposal = await Proposal.create({
    refId: jobId,
    bidAmmount,
    coverLetter,
    proposedDuration,
    attachments,
    type: "job",
  });

  await new ProposalCreatedPublisher(natsWrapper.client)
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

const createServiceProposal = catchAsync(async (req, res, next) => {
  const serviceId = req.params.id;

  const service = await Job.findById(serviceId);

  if (!service) {
    next(new AppError(`No service found with id: ${serviceId}`, 204));
  }

  const { bidAmmount, coverLetter, proposedDuration, attachments } = req.body;

  if (bidAmmount > service.budget) {
    next(
      new AppError(
        "Bid amount should be less then or equal to the service budget.",
        400
      )
    );
  }

  const proposal = await Proposal.create({
    refId: serviceId,
    bidAmmount,
    coverLetter,
    proposedDuration,
    attachments,
    type: "service",
  });

  await new ProposalCreatedPublisher(natsWrapper.client)
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

const getJobProposals = catchAsync(async (req, res, next) => {
  const jobId = req.params.id;

  const job = await Job.findById(jobId);

  if (!job) {
    next(new AppError(`No job found with id: ${jobId}`, 204));
  }

  const isAllowed = req.currentUser.id === job.userId.toString();

  if (!isAllowed) {
    next(
      new AppError("You don't have permission to perform this operation.", 403)
    );
  }

  const proposal = await Proposal.find({
    $and: [{ type: "job" }, { refId: jobId }],
  });

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
    next(new AppError(`No service found with id: ${serviceId}`, 204));
  }

  const isAllowed = req.currentUser.id === service.userId.toString();

  if (!isAllowed) {
    next(
      new AppError("You don't have permission to perform this operation.", 403)
    );
  }

  const proposal = await Proposal.find({
    $and: [{ type: "service" }, { refId: serviceId }],
  });

  res.status(200).json({
    status: "success",
    data: {
      proposal,
    },
  });
});

const getProposal = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const proposal = await Proposal.findById(id);

  if (!proposal) {
    next(new AppError(`No proposal found with id: ${id}`, 204));
  }

  let isAllowed = await checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    next(new AppError("Your are not allowed to perform this action.", 403));
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
    next(new AppError(`No proposal found with id: ${id}`, 204));
  }

  let isAllowed = checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    next(new AppError("Your are not allowed to perform this action.", 403));
  }

  const isProposalOwner = req.currentUser.id === proposal.userId.toString();

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
      new AppError(`No proposal found with matching id: ${proposalId}`, 204)
    );
  }

  const isAllowed = req.currentUser.id === proposal.userId.toString();

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

  res.status(200).json({
    status: "success",
    data: {
      proposal,
    },
  });
});

const getProposalAttachments = catchAsync(async (req, res, next) => {
  const proposalId = req.params.id;

  const proposal = await Proposal.findById(proposalId);

  if (!proposal) {
    return next(
      new AppError(`No proposal found with matching id: ${proposalId}`, 204)
    );
  }

  const isAllowed = await checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    next(new AppError("You dont have permission to perform this action.", 403));
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
    return next(new AppError(`No proposal found with matching id: ${id}`, 204));
  }

  const isAllowed = await checkUserPermission(req.currentUser, proposal);

  if (!isAllowed) {
    next(new AppError("Your are not allowed to perform this action.", 403));
  }

  const attachment = proposal.attachments.find(
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

const deleteProposalAttachment = catchAsync(async (req, res, next) => {
  const { id, attachmentid } = req.params;

  const proposal = await Proposal.findById(id);

  if (!proposal) {
    return next(new AppError(`No proposal found with matching id: ${id}`, 204));
  }

  const isAllowed = req.currentUser.id === proposal.userId.toString();

  if (!isAllowed) {
    next(new AppError("You dont have permission to perform this action.", 403));
  }

  const attachmentIndex = proposal.attachments.findIndex(
    (attachment) => attachment._id.toString() === attachmentid
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
    return next(new AppError(`No proposal found with matching id: ${id}`, 204));
  }

  const isAllowed = req.currentUser.id === proposal.userId.toString();

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
