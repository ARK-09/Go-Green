const crypto = require("crypto");

const encryptToken = (token) => {
  const encryptedToken = crypto
    .createHash("sha256")
    .update(token)
    .digest("hex");

  return encryptedToken;
};

const generateResetToken = () => {
  const resetToken = crypto.randomBytes(32).toString("hex");
  const hashedToken = encryptToken(resetToken);
  return { resetToken, hashedToken };
};

exports.encryptToken = encryptToken;
exports.generateResetToken = generateResetToken;
