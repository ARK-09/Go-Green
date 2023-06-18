const express = require("express");
const {
  globalErrorController,
  AppError,
} = require("@ark-industries/gogreen-common");

const messageRouter = require("./routes/messageRouter");
const roomRouter = require("./routes/roomRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/chats/messages", messageRouter);
app.use("/api/v1/chats/rooms", roomRouter);

app.use("*", (req, res, next) => {
  next(new AppError(`Can't find path: ${req.path} on this server`, 404));
});

app.use(globalErrorController);

module.exports = app;
