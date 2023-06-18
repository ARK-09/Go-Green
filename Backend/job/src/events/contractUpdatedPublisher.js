const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class ContractUpdatedPublisher extends Publisher {
  subject = Subjects.contractUpdated;
}

module.exports = ContractUpdatedPublisher;
