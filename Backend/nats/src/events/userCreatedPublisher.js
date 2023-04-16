const Publisher = require("./publisher");
const Subejcts = require("./subjects");

class UserCreatedPulisher extends Publisher {
  subejct = Subejcts.userCreated;
}

module.exports = UserCreatedPulisher;
