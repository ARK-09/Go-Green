const Email = require("./email");

class EmailBuilder {
  constructor() {
    this.from = null;
    this.to = null;
    this.subject = null;
    this.text = null;
    this.html = null;
    this.sender = null;
  }

  setFrom(from) {
    this.from = from;
    return this;
  }

  setTo(to) {
    this.to = to;
    return this;
  }

  setSubject(subject) {
    this.subject = subject;
    return this;
  }

  setText(text) {
    this.text = text;
    return this;
  }

  setHtml(html) {
    this.html = html;
    return this;
  }

  setTransporter(transporter) {
    this.transporter = transporter;
    return this;
  }

  build() {
    return new Email(
      this.from,
      this.to,
      this.subject,
      this.text,
      this.html,
      this.transporter
    );
  }
}

module.exports = EmailBuilder;
