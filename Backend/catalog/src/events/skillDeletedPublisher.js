const { Subjects, Publisher } = require("@ark-industries/gogreen-common");

class SkillDeletedPublisher extends Publisher {
  subject = "skill:deleted";
}

module.exports = SkillDeletedPublisher;
