const jwt = require("jsonwebtoken");

const JWT_KEY = process.env.JWT_KEY;
const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY) / (24 * 60 * 60); // recieving 10 days in seconds converting to 10 day

class Jwt {
  static sign(payload) {
    return jwt.sign(payload, JWT_KEY, {
      expiresIn: JWT_EXPIRY + "d",
    });
  }

  static verify(JWT) {
    return jwt.verify(JWT, JWT_KEY);
  }
}

module.exports = Jwt;
