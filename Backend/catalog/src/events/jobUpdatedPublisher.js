const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class JobUpdatedPublisher extends Publisher {
  subject = Subjects.jobUpdated;
}

module.exports = JobUpdatedPublisher;
