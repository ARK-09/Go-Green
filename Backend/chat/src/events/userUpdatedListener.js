const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

class UserUpdatedListener extends Listener {
  subject = Subjects.userUpdated;
  queueGroupName = "chat-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const {
        id,
        name,
        email,
        password,
        passwordChangedAt,
        userType,
        phoneNo,
        image,
      } = data;

      await User.findByIdAndUpdate(
        id,
        {
          name,
          email,
          password,
          passwordChangedAt,
          userType,
          phoneNo,
          image,
        },
        { runValidators: false }
      );

      message.ack();
    }
  };
}

module.exports = UserUpdatedListener;
