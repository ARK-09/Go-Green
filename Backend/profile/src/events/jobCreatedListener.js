const { Listener } = require("@ark-industries/gogreen-common");
const Job = require("../models/jobs");

class JobCreatedListener extends Listener {
  subject = "job:created";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      await Job.create(data);
      message.ack();
    }
  };
}

module.exports = JobCreatedListener;
