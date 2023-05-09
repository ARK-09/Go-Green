const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");
const Profile = require("../models/profiles");

class UserDeletedListener extends Listener {
  subject = Subjects.userDeleted;
  queueGroupName = "auth-service-queue-group";

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

      await Profile.findOneAndUpdate(
        { userId: id },
        { active: false },
        { runValidators: false }
      );

      message.ack();
    }
  };
}

module.exports = UserDeletedListener;
