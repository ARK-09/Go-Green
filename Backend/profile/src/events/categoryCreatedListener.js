const { Listener } = require("@ark-industries/gogreen-common");
const Category = require("../models/categories");

class CategoryCreatedListener extends Listener {
  subject = "category:created";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      await Category.create(data);
      message.ack();
    }
  };
}

module.exports = CategoryCreatedListener;
