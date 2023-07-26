const Job = require("../models/jobs");

async function checkUserPermission(currentUser, proposal) {
  const proposalUserId = proposal.user.toString();
  const currentUserIsProposalUser = currentUser.id === proposalUserId;

  if (currentUserIsProposalUser) {
    return true;
  }

  const proposalType = proposal.type;

  if (proposalType === "job") {
    const job = await Job.findById(proposal.refId);
    return currentUser.id === job.user.toString();
  } else if (proposalType === "service") {
    const service = await Job.findById(proposal.refId);
    return currentUser.id === service.user.toString();
  }

  return false;
}

async function updateProposalStatus(proposal, status) {
  proposal.status = status;
  await proposal.save();
}

exports.checkUserPermission = checkUserPermission;
exports.updateProposalStatus = updateProposalStatus;
