const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class UserResetPasswordPublisher extends Publisher {
  subject = Subjects.userResetPassword;
}

module.exports = UserResetPasswordPublisher;
