const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class CategoryUpdatedPublisher extends Publisher {
  subject = "category:updated";
}

module.exports = CategoryUpdatedPublisher;
