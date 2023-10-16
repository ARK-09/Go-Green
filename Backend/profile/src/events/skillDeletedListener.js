const { Listener } = require("@ark-industries/gogreen-common");
const Skill = require("../models/skills");

class SkillDeletedListener extends Listener {
  subject = "skill:deleted";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      if (data.all) {
        await Skill.deleteMany({});
        message.ack();
        return;
      }

      const id = data;
      await Skill.findByIdAndDelete(id);
      message.ack();
    }
  };
}

module.exports = SkillDeletedListener;
