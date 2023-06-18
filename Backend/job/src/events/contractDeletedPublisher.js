const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class ContractDeletedPublisher extends Publisher {
  subject = Subjects.contractDeleted;
}

module.exports = ContractDeletedPublisher;
