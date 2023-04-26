const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class UserCreatedPublisher extends Publisher {
  subject = Subjects.userCreated;
}

module.exports = UserCreatedPublisher;
