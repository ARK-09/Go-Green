const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

class UserForgetPasswordListener extends Listener {
  subject = Subjects.userForgetPassword;
  queueGroupName = "chat-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const { id, resetToken, resetTokenExpireAt } = data;

      await User.findByIdAndUpdate(
        id,
        {
          resetToken,
          resetTokenExpireAt,
        },
        { runValidators: false }
      );

      message.ack();
    }
  };
}

module.exports = UserForgetPasswordListener;
