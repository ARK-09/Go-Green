const { scrypt, randomBytes } = require("crypto");
const { promisify } = require("util");

const scryptAsync = promisify(scrypt);

class Password {
  static async toHash(password) {
    if (typeof password !== "string") return;

    const salt = randomBytes(9).toString("hex");
    const buf = await scryptAsync(password, salt, 64);

    return `${buf.toString("hex")}.${salt}`;
  }

  static async compare(storedPassword, suppliedPassword) {
    if (typeof suppliedPassword !== "string") return false;

    const [hashedPassword, salt] = storedPassword.split(".");
    const buf = await scryptAsync(suppliedPassword, salt, 64);

    return buf.toString("hex") === hashedPassword;
  }
}

module.exports = Password;
