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
    host: process.env.MAIL_TRANSPORTER_HOST_DEV,
    port: process.env.MAIL_TRANSPORTER_HOST_PORT_DEV,
    auth: {
      user: process.env.MAIL_TRANSPORTER_USER_NAME_DEV,
      pass: process.env.MAIL_TRANSPORTER_USER_PASSWORD_DEV,
    },
  });
};

const productionTransporter = () => {
  return new NodeMailer({
    host: process.env.MAIL_TRANSPORTER_HOST_PROD,
    port: process.env.MAIL_TRANSPORTER_HOST_PORT_PROD,
    auth: {
      user: process.env.MAIL_TRANSPORTER_USER_NAME_PROD,
      pass: process.env.MAIL_TRANSPORTER_USER_PASSWORD_PROD,
    },
  });
};

exports.productionTransporter = productionTransporter;
exports.devTransporter = devTransporter;
