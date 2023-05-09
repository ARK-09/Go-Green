const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class UserUpdatedPublisher extends Publisher {
  subject = Subjects.userUpdated;
}

module.exports = UserUpdatedPublisher;
