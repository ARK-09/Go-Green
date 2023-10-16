const { Listener } = require("@ark-industries/gogreen-common");
const Category = require("../models/categories");

class CategoryUpdatedListener extends Listener {
  subject = "category:updated";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const { _id, title } = data;

      await Category.findByIdAndUpdate(_id, { title: title });

      message.ack();
    }
  };
}

module.exports = CategoryUpdatedListener;
