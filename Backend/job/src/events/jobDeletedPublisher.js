const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class JobDeletedPublisher extends Publisher {
  subject = Subjects.jobDeleted;
}

module.exports = JobDeletedPublisher;
