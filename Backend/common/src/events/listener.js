class Listener {
  subject;
  queueGroupName;
  client;
  ackWait = 5 * 1000;
  #client;

  constructor(client) {
    this.#client = client;
  }

  subscriptionOptions = () => {
    return this.#client
      .subscriptionOptions()
      .setDeliverAllAvailable()
      .setAckWait(this.ackWait)
      .setDurableName(this.queueGroupName);
  };

  listen = () => {
    if (!this.subject) {
      throw new Error("Subject must be supplied.");
    }

    const subsription = this.#client.subscribe(
      this.subject,
      this.queueGroupName,
      this.subscriptionOptions()
    );

    subsription.on("message", (msg) => {
      console.log(`Message received: ${this.subject} / ${this.queueGroupName}`);

      const parsedData = this.parseMessage(msg);
      this.onMessage(parsedData, msg);
    });
  };

  parseMessage = (message) => {
    const data = msg.getData();

    return typeof data === "string"
      ? JSON.parse(data)
      : JSON.parse(data.toString("utf8"));
  };

  onMessage = async (parsedData, message) => {};
}

module.exports = Listener;
