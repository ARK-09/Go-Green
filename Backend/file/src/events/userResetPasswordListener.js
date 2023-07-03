const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

class UserResetPasswordListener extends Listener {
  subject = Subjects.userResetPassword;
  queueGroupName = "file-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const { id, newPassword, passwordChangedAt } = data;

      await User.findByIdAndUpdate(
        id,
        {
          password: newPassword,
          passwordChangedAt,
          resetToken: null,
          resetTokenExpireAt: null,
        },
        { runValidators: false }
      );

      message.ack();
    }
  };
}

module.exports = UserResetPasswordListener;
