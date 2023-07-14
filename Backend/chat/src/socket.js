const { createServer } = require("http");
const io = require("socket.io");

class Socket {
  constructor(server, options = {}) {
    this.server = createServer(server);
    this.io = io(this.server, options);
  }
}

module.exports = Socket;
