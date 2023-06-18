const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

class UserDeletedListener extends Listener {
  subject = Subjects.userDeleted;
  queueGroupName = "chat-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const { id } = data;

      await User.findByIdAndUpdate(
        id,
        { isActive: false, userStatus: "Offline" },
        { runValidators: false }
      );

      message.ack();
    }
  };
}

module.exports = UserDeletedListener;
