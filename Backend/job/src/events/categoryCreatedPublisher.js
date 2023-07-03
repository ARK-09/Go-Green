const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class CategoryCreatedPublisher extends Publisher {
  subject = "category:created";
}

module.exports = CategoryCreatedPublisher;
