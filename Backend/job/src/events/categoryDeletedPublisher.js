const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class CategoryDeletedPublisher extends Publisher {
  subject = "category:deleted";
}

module.exports = CategoryDeletedPublisher;
