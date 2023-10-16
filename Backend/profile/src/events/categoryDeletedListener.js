const { Listener } = require("@ark-industries/gogreen-common");
const Category = require("../models/categories");

class CategoryDeletedListener extends Listener {
  subject = "category:deleted";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      if (data.all) {
        await Category.deleteMany({});
        message.ack();
        return;
      }

      const id = data;
      await Category.findByIdAndDelete(id);
      message.ack();
    }
  };
}

module.exports = CategoryDeletedListener;
