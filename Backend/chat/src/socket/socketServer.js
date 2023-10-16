const { createServer } = require("http");
const io = require("socket.io");

class SocketServer {
  static instance = null;
  io = null;
  server = null;

  constructor(server, options) {
    if (!SocketServer.instance) {
      this.server = createServer(server);
      this.io = io(this.server, options);
      SocketServer.instance = this;
    }
  }

  use(...middlewares) {
    if (!SocketServer.instance) {
      throw new Error("Can't add middlewares before initializing the socket.");
    }

    middlewares.forEach((middleware) => {
      this.io.use(middleware);
    });
  }

  listen(callback) {
    if (!SocketServer.instance) {
      throw new Error("Can't listen before initializing the socket.");
    }

    this.io.on("connection", (socket) => {
      callback(socket);
    });

    return this.server;
  }

  static getIo() {
    if (!SocketServer.instance) {
      throw new Error("Can't get io before initialization.");
    }

    return this.instance.io;
  }
}

module.exports = SocketServer;
