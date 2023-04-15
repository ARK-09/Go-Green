const nodemailer = require("nodemailer");

class NodeMailer {
  constructor(transporterOptions) {
    this.transporter = nodemailer.createTransport(transporterOptions);
  }

  async sendMail(mailOptions) {
    return await this.transporter.sendMail(mailOptions);
  }
}

const devTransporter = () => {
  return new NodeMailer({
    host: "sandbox.smtp.mailtrap.io",
    port: 2525,
    auth: {
      user: "fb985c752b398c",
      pass: "85c9d40f670709",
    },
  });
};

const productionTransporter = () => {
  return new NodeMailer({
    host: "sandbox.smtp.mailtrap.io",
    port: 2525,
    auth: {
      user: "fb985c752b398c",
      pass: "85c9d40f670709",
    },
  });
};

exports.productionTransporter = productionTransporter;
exports.devTransporter = devTransporter;
