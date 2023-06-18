const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class ContractCreatedPublisher extends Publisher {
  subject = Subjects.contractCreated;
}

module.exports = ContractCreatedPublisher;
