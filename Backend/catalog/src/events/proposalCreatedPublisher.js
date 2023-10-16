const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class ProposalCreatedPublisher extends Publisher {
  subject = Subjects.proposalCreated;
}

module.exports = ProposalCreatedPublisher;
