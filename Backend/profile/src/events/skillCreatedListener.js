const { Listener } = require("@ark-industries/gogreen-common");
const Skill = require("../models/skills");

class SkillCreatedListener extends Listener {
  subject = "skill:created";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      await Skill.create(data);
      message.ack();
    }
  };
}

module.exports = SkillCreatedListener;
