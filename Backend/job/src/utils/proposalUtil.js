const Job = require("../models/jobs");

async function checkUserPermission(currentUser, proposal) {
  const proposalUserId = proposal.user.toString();
  const currentUserIsProposalUser =
    currentUser._id.toString() === proposalUserId;

  if (currentUserIsProposalUser) {
    return true;
  }

  console.log(currentUserIsProposalUser);

  const proposalType = proposal.type;

  if (proposalType === "Jobs") {
    const job = await Job.findById(proposal.doc);
    return currentUser._id.toString() === job.user.toString();
  } else if (proposalType === "Services") {
    const service = await Job.findById(proposal.doc);
    return currentUser._id.toString() === service.user.toString();
  }

  return false;
}

async function updateProposalStatus(proposal, status) {
  proposal.status = status;
  await proposal.save();
}

exports.checkUserPermission = checkUserPermission;
exports.updateProposalStatus = updateProposalStatus;
