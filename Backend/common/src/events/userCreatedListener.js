const Listener = require("../events/listener");
const Subejcts = require("./subjects");

class UserCreatedListner extends Listener {
  subject = Subejcts.userCreated;
  queueGroupName = "auth-service-queue-group";

  onMessage(data, message) {
    message.ack();
  }
}

module.exports = UserCreatedListner;
