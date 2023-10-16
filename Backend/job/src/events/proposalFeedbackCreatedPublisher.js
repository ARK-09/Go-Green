const { Publisher } = require("@ark-industries/gogreen-common");

class ProposalFeedbackCreatedPublisher extends Publisher {
  subject = "proposal:feedback-created";
}

module.exports = ProposalFeedbackCreatedPublisher;
