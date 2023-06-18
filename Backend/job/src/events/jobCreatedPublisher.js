const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class JobCreatedPublisher extends Publisher {
  subject = Subjects.jobCreated;
}

module.exports = JobCreatedPublisher;
