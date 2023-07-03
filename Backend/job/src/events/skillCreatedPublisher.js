const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class SkillCreatedPublisher extends Publisher {
  subject = "skill:created";
}

module.exports = SkillCreatedPublisher;
