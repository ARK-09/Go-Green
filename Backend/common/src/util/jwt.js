const jwt = require("jsonwebtoken");

class Jwt {
  static sign(payload, JWT_KEY, JWT_EXPIRY) {
    return jwt.sign(payload, JWT_KEY, {
      expiresIn: JWT_EXPIRY,
    });
  }

  static verify(JWT, JWT_KEY) {
    return jwt.verify(JWT, JWT_KEY);
  }
}

module.exports = Jwt;
