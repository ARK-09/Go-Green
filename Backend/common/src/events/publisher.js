class Publisher {
  subject;
  #client;

  constructor(client) {
    this.#client = client;
  }

  publish(data) {
    return new Promise((resolve, rejects) => {
      this.#client.publish(this.subject, JSON.stringify(data), (err) => {
        if (err) {
          return rejects(err);
        }

        console.log("Event published to subject", this.subject);
        resolve();
      });
    });
  }
}

module.exports = Publisher;
