const { Listener } = require("@ark-industries/gogreen-common");
const Skill = require("../models/skills");

class SkillUpdatedListener extends Listener {
  subject = "skill:updated";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      const { _id, title, categories } = data;

      await Skill.findByIdAndUpdate(_id, { title, categories });

      message.ack();
    }
  };
}

module.exports = SkillUpdatedListener;
