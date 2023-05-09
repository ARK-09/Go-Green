const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class UserForgetPasswordPublisher extends Publisher {
  subject = Subjects.userForgetPassword;
}

module.exports = UserForgetPasswordPublisher;
