const { Listener } = require("@ark-industries/gogreen-common");
const Proposal = require("../models/proposals");

class ProposalCreatedListener extends Listener {
  subject = "proposal:created";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      await Proposal.create(data);
      message.ack();
    }
  };
}

module.exports = ProposalCreatedListener;
