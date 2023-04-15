const { convert } = require("html-to-text");

class Email {
  constructor(from, to, subject, text, html, sender) {
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.text = text;
    this.html = html;
    this.sender = sender;
  }

  async sendMail() {
    const mailOptions = {
      from: this.from,
      to: this.to,
      subject: this.subject,
      text: this.html ? convert(this.html) : this.text,
      html: this.html,
    };

    return await this.sender.sendMail(mailOptions);
  }
}

module.exports = Email;
