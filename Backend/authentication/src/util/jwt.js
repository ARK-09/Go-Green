const jwt = require("jsonwebtoken");

const JWT_KEY = process.env.JWT_KEY;
const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY); // miliseconds

class Jwt {
  static sign(payload) {
    const payloadObject = { ...payload };

    return jwt.sign(payloadObject, JWT_KEY, {
      expiresIn: JWT_EXPIRY,
    });
  }

  static verify(JWT) {
    return jwt.verify(JWT, JWT_KEY);
  }
}

module.exports = Jwt;
