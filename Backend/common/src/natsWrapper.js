const nats = require("node-nats-streaming");

class NatsWrapper {
  #client;

  get client() {
    if (!this.#client) {
      throw new Error("Cannot access NATS client before connecting");
    }

    return this.#client;
  }

  connect(clusterId, clientId, url) {
    this.#client = nats.connect(clusterId, clientId, { url });

    // this.#client.on("close", () => {
    //   console.log("NATAS connection closed!");
    //   process.exit();
    // });

    // process.on("SIGINT", () => this.#client.close());
    // process.on("SIGTERM", () => this.#client.close());

    return new Promise((resolve, reject) => {
      this.#client.on("connect", () => {
        console.log("Connected to NATS");
        resolve();
      });

      this.#client.on("error", (err) => {
        reject(err);
      });
    });
  }
}

module.exports = new NatsWrapper();
