const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

class UserCreatedListener extends Listener {
  subject = Subjects.userCreated;
  queueGroupName = "file-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const {
        _id,
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
            _id: _id,
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
      message.ack();
    }
  };
}

module.exports = UserCreatedListener;
