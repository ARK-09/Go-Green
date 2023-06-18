const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class ProposalDeletedPublisher extends Publisher {
  subject = Subjects.proposalDeleted;
}

module.exports = ProposalDeletedPublisher;
