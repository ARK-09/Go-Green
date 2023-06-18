const Contract = require("../models/contracts");
const Job = require("../models/jobs");
const Proposal = require("../models/proposals");
const {
  catchAsync,
  AppError,
  natsWrapper,
} = require("@ark-industries/gogreen-common");
const ContractCreatedPublisher = require("../events/contractCreatedPublisher");
const ContractUpdatedPublisher = require("../events/contractUpdatedPublisher");
const ContractDeletedPublisher = require("../events/contractDeletedPublisher");

const createContract = catchAsync(async (req, res, next) => {
  const { proposalid, ammount } = req.body;

  const proposal = await Proposal.findById(proposalid);

  if (!proposal) {
    next(new AppError(`No proposal found with id: ${proposalid}.`, 204));
  }

  const job = await Job.findById(proposal.refId);

  const isAllowed =
    (req.currentUser.id === proposal.userId.toString() &&
      proposal.status === "Hired") ||
    req.currentUser.id === job.userId.toString();

  if (!isAllowed) {
    next(new AppError("You dont have permession to perform this action.", 403));
  }

  const contract = await Contract.create({
    userId: req.currentUser.id,
    proposalId: proposalid,
    ammount,
  }).lean();

  await new ContractCreatedPublisher(natsWrapper.client)
    .publish(contract)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: {
      contract,
    },
  });
});

const getContracts = catchAsync(async (req, res, next) => {
  const contracts = await Contract.find({ userId: req.currentUser.id });
  res.status(200).json({
    status: "success",
    data: {
      contracts,
    },
  });
});

const getContract = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const contract = await Contract.findById(id);

  if (!contract) {
    next(new AppError(`No contract found with id: ${id}`, 403));
  }

  const proposal = await Proposal.findOne({ _id: contract.proposalId });

  const isAllowed =
    req.currentUser.id === contract.userId.toString() ||
    req.currentUser.id === proposal.userId.toString();

  if (!isAllowed) {
    next(new AppError(`You dont have permission to perform this action.`, 403));
  }

  res.status(200).json({
    status: "success",
    data: {
      contract,
    },
  });
});

const updateContract = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const contract = await Contract.findById(id);

  if (!contract) {
    next(new AppError(`No contract found with id: ${id}`, 403));
  }

  const proposal = await Proposal.findOne({ _id: contract.proposalId });

  const isAllowed =
    req.currentUser.id === contract.userId.toString() ||
    req.currentUser.id === proposal.userId.toString();

  if (!isAllowed) {
    next(new AppError(`You dont have permission to perform this action.`, 403));
  }

  const { ammount, status } = req.body;

  contract.ammount = ammount || contract.ammount;
  contract.status = status || contract.status;
  await contract.save();

  await new ContractUpdatedPublisher(natsWrapper.client)
    .publish(contract)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: {
      contract,
    },
  });
});

const deleteContract = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const contract = await Contract.findById(id);

  if (!contract) {
    next(new AppError(`No contract found with id: ${id}`, 403));
  }

  const proposal = await Proposal.findOne({ _id: contract.proposalId });

  const isAllowed =
    req.currentUser.id === contract.userId.toString() ||
    req.currentUser.id === proposal.userId.toString();

  if (!isAllowed) {
    next(new AppError(`You dont have permission to perform this action.`, 403));
  }

  await Contract.findByIdAndDelete(contract._id);

  await new ContractDeletedPublisher(natsWrapper.client)
    .publish({ id: contract._id })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: null,
  });
});

exports.createContract = createContract;
exports.getContracts = getContracts;
exports.getContract = getContract;
exports.updateContract = updateContract;
exports.deleteContract = deleteContract;
