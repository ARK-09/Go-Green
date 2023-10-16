const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class SkillUpdatedPublisher extends Publisher {
  subject = "skill:updated";
}

module.exports = SkillUpdatedPublisher;
