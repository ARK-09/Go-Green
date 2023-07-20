// require("dotenv").config({ path: "./config.env" });

const mongoose = require("mongoose");
const app = require("./app");
const SocketServer = require("./socket/socketServer");
const socketAuth = require("./middelwares/socketAuth");
const socketCurrentUser = require("./middelwares/socketCurrentUser");
const Listeners = require("./socket/events/listeners/listener");
const { natsWrapper } = require("@ark-industries/gogreen-common");
const UserCreatedListener = require("./events/userCreatedListener");
const UserUpdatedListener = require("./events/userUpdatedListener");
const UserDeletedListener = require("./events/userDeletedListener");
const UserForgetPasswordListener = require("./events/userForgetPasswordListener");
const UserResetPasswordListener = require("./events/userResetPasswordListener");

natsWrapper.connect("gogreen", "1112", "http://nats-srv:4222").then(() => {
  new UserCreatedListener(natsWrapper.client).listen();
  new UserUpdatedListener(natsWrapper.client).listen();
  new UserDeletedListener(natsWrapper.client).listen();
  new UserForgetPasswordListener(natsWrapper.client).listen();
  new UserResetPasswordListener(natsWrapper.client).listen();

  natsWrapper.client.on("close", () => {
    console.log("NATAS connection closed!");
    process.exit();
  });

  process.on("SIGINT", () => natsWrapper.client.close());
  process.on("SIGTERM", () => natsWrapper.client.close());
});

const connectionString = process.env.MONGO_URI;

mongoose.connect(connectionString).then(() => {
  console.log("DB connection successful!");
});

const socketServer = new SocketServer(app, {
  path: "/api/v1/chats",
  cors: {
    origin: "https://www.gogreen.com",
    methods: ["GET", "POST"],
  },
});

socketServer.use(socketAuth, socketCurrentUser);

const httpServer = socketServer.listen(() => {
  Listeners.init();
});

const port = parseInt(process.env.PORT) || 4005;

const server = httpServer.listen(port, () => {
  console.log(`Listening at port:${port}`);
});

process.on("uncaughtException", (err) => {
  console.log(err, err.name, err.message);
  console.log("uncaughtException! Shutting down...");

  server.close(() => {
    process.exit(1);
  });
});

process.on("unhandledRejection", (err) => {
  console.log(err.name, err.message, err.stack);
  console.log("unhandledRejection! Shutting down...");

  server.close(() => {
    process.exit(1);
  });
});
