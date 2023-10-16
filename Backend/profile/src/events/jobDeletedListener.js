const { Listener } = require("@ark-industries/gogreen-common");
const Job = require("../models/jobs");

class JobDeletedListener extends Listener {
  subject = "job:deleted";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      if (data.all) {
        await Job.deleteMany({});
        message.ack();
        return;
      }

      await Job.create(data);
      message.ack();
    }
  };
}

module.exports = JobDeletedListener;
