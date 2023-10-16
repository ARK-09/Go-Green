const { Listener } = require("@ark-industries/gogreen-common");
const Job = require("../models/jobs");

class JobUpdatedListener extends Listener {
  subject = "job:updated";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const { _id } = data;

      await Job.findByIdAndUpdate(_id, { ...data });

      message.ack();
    }
  };
}

module.exports = JobUpdatedListener;
