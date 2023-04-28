const { Listener, Subjects } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

class UserCreatedListener extends Listener {
  subject = Subjects.userCreated;
  queueGroupName = "auth-service-queue-group";

  constructor(client) {
    super(client);
  }

  // name, email, password, passwordChangedAt, resetTokken, resetTokkenGeneratedAt, otp, otpGeneratedAt, isActive, invalidLoginCount,
  // userType, phoneNo, image, userStatus, verified, joinedDate, financeAllowed, blocked
  onMessage = async (data, message) => {
    return new Promise((resolve, reject) => {
      if (data) {
        console.log(data);
        message.ack();
        resolve(data);
      }
    });
  };
}

module.exports = UserCreatedListener;
