const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class UserDeletedPublisher extends Publisher {
  subject = Subjects.userDeleted;
}

module.exports = UserDeletedPublisher;
