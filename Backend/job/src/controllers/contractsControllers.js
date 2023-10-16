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

const createContract = catchAsync(async (req, res, next) => {
  const { proposalid, amount } = req.body;

  const proposal = await Proposal.findById(proposalid);

  if (!proposal) {
    return next(new AppError(`No proposal found with id: ${proposalid}.`, 404));
  }

  const job = await Job.findById(proposal.doc);

  const isProposalOwner =
    req.currentUser._id.toString() === proposal.user.toString();
  const isJobOwner = req.currentUser._id.toString() === job.user.toString();

  const isAllowed = isProposalOwner || isJobOwner;

  if (!isAllowed) {
    return next(
      new AppError("You dont have permession to perform this action.", 403)
    );
  }

  if (isProposalOwner && proposal.status != "Hired") {
    return next(
      new AppError("You dont have permession to perform this action.", 403)
    );
  } else if (isJobOwner && proposal.status != "Hired") {
    return next(new AppError("Please hire the proposal first.", 403));
  }

  const contract = await Contract.create({
    user: req.currentUser._id.toString(),
    proposalId: proposalid,
    amount,
    status: isJobOwner
      ? "In Progress"
      : isProposalOwner
      ? "Pending Approval"
      : undefined,
  });

  await new ContractCreatedPublisher(natsWrapper.client)
    .publish(contract)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const contractsPopulated = await contract.populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  res.status(200).json({
    status: "success",
    data: {
      contract: contractsPopulated,
    },
  });
});

const getContracts = catchAsync(async (req, res, next) => {
  const contracts = await Contract.find({
    user: req.currentUser._id.toString(),
  }).populate("user", `-${fieldsToExclude.join(" -")}`);

  res.status(200).json({
    status: "success",
    data: {
      contracts,
    },
  });
});

const getJobContracts = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const job = await Job.findById(id);

  if (!job) {
    return next(new AppError(`No job found with matching id: ${id}`, 404));
  }

  const proposal = await Proposal.findOne({
    type: "job",
    doc: id,
    status: "Hired",
  });

  const isJobOwner = req.currentUser._id.toString() === job.user.toString();
  const isHiredForTheJob = !proposal
    ? false
    : proposal.user.toString() === req.currentUser._id.toString();

  let searchQuery;

  if (isJobOwner) {
    searchQuery = {
      $or: [{ user: req.currentUser._id.toString() }, { user: proposal.user }],
    };
  } else if (isHiredForTheJob) {
    searchQuery = { user: req.currentUser._id.toString() };
  }

  const isAllowed = isJobOwner || isHiredForTheJob;

  if (!isAllowed) {
    return next(new AppError("You'r not allowed to perform this action.", 403));
  }

  const contracts = await Contract.find(searchQuery).populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  return res.status(200).json({
    status: "success",
    data: {
      contracts,
    },
  });
});

const getContract = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const contract = await Contract.findById(id).populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  if (!contract) {
    return next(new AppError(`No contract found with id: ${id}`, 403));
  }

  const proposal = await Proposal.findOne({ _id: contract.proposalId });

  const isAllowed =
    req.currentUser._id.toString() === contract.user.toString() ||
    req.currentUser._id.toString() === proposal.user.toString();

  if (!isAllowed) {
    return next(
      new AppError(`You dont have permission to perform this action.`, 403)
    );
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
    return next(new AppError(`No contract found with id: ${id}`, 403));
  }

  const proposal = await Proposal.findOne({ _id: contract.proposalId });

  const isContractOwner =
    contract.user.toString() === req.currentUser._id.toString();
  const isContractPartner =
    req.currentUser._id.toString() === proposal.user.toString();

  const isAllowed = isContractOwner || isContractPartner;

  if (!isAllowed) {
    return next(
      new AppError(`You dont have permission to perform this action.`, 403)
    );
  }

  const { amount, status } = req.body;

  if (isContractOwner) {
    if (amount) {
      if (amount > 5) {
        contract.amount = amount;
      } else {
        return next(new AppError("Contract amount should be > 5", 400));
      }
    }

    if (!amount && status) {
      if (["Revision", "Completed", "Canceled"].includes(status)) {
        contract.status = status;
      } else {
        return next(
          new AppError(
            "Contract status should be 'Revision', 'Completed', 'Canceled'",
            400
          )
        );
      }
    }
  } else if (isContractPartner) {
    if (amount) {
      if (amount > 5) {
        contract.amount = amount;
        contract.status = "Pending Approval";
      } else {
        return next(new AppError("Contract amount should be > 5", 400));
      }
    }

    if (!amount && status) {
      if (status === "Delivered") {
        contract.status = status;
      } else {
        return next(new AppError("Contract status should be 'Delivered'", 400));
      }
    }
  }

  await contract.save();

  await new ContractUpdatedPublisher(natsWrapper.client)
    .publish(contract)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const contractPopulated = await contract.populate(
    "user",
    `-${fieldsToExclude.join(" -")}`
  );

  res.status(200).json({
    status: "success",
    data: {
      contract: contractPopulated,
    },
  });
});

const deleteContract = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const contract = await Contract.findById(id);

  if (!contract) {
    return next(new AppError(`No contract found with id: ${id}`, 403));
  }

  const proposal = await Proposal.findOne({ _id: contract.proposalId });

  const isAllowed =
    req.currentUser._id.toString() === contract.user.toString() ||
    req.currentUser._id.toString() === proposal.user.toString();

  if (!isAllowed) {
    return next(
      new AppError(`You dont have permission to perform this action.`, 403)
    );
  }

  await Contract.findByIdAndDelete(contract._id);

  await new ContractDeletedPublisher(natsWrapper.client)
    .publish({ id: contract._id })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(204).json({
    status: "success",
    data: null,
  });
});

exports.createContract = createContract;
exports.getContracts = getContracts;
exports.getJobContracts = getJobContracts;
exports.getContract = getContract;
exports.updateContract = updateContract;
exports.deleteContract = deleteContract;
