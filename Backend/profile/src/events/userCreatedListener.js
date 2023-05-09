const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");
const Profile = require("../models/profiles");

class UserCreatedListener extends Listener {
  subject = Subjects.userCreated;
  queueGroupName = "auth-service-queue-group";

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
        userType,
        phoneNo,
        image,
        passwordChangedAt,
        joinedDate,
      } = data;

      await User.create(
        [
          {
            _id: id,
            name,
            email,
            password,
            userType,
            phoneNo,
            image,
            passwordChangedAt,
            joinedDate,
          },
        ],
        { validateBeforeSave: false }
      );
      await Profile.create([{ userId: id }], { validateBeforeSave: false });

      message.ack();
    }
  };
}

module.exports = UserCreatedListener;
