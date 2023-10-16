const { Listener } = require("@ark-industries/gogreen-common");
const Proposal = require("../models/proposals");

class ProposalUpdatedListener extends Listener {
  subject = "proposal:updated";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const { _id } = data;

      await Proposal.findByIdAndUpdate(_id, { ...data });

      message.ack();
    }
  };
}

module.exports = ProposalUpdatedListener;
