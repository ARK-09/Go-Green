const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

class UserCreatedListener extends Listener {
  subject = Subjects.userCreated;
  queueGroupName = "auth-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    return new Promise((resolve, reject) => {
      console.log(data);
      message.ack();
      resolve(data);
      if (data) {
      }
    });
  };
}

module.exports = UserCreatedListener;
