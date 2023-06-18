const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class ProposalUpdatedPublisher extends Publisher {
  subject = Subjects.proposalUpdated;
}

module.exports = ProposalUpdatedPublisher;
