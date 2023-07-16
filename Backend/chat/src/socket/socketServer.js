const { createServer } = require("http");
const io = require("socket.io");

class SocketServer {
  static instance = null;
  server = null;
  io = null;
  socket = null;

  static getInstance(server, options = {}) {
    if (!SocketServer.instance) {
      SocketServer.instance = new SocketServer(server, options);
    }
    return SocketServer.instance;
  }

  constructor(server, options = {}) {
    if (SocketServer.instance) {
      throw new Error(
        "Singleton class, use getInstance() to get the instance."
      );
    }
    this.server = createServer(server);
    this.io = io(this.server, options);
    this.socket = null;
    SocketServer.instance = this;
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
      this.socket = socket;
      callback(socket);
    });

    return this.server;
  }
}

module.exports = SocketServer;
