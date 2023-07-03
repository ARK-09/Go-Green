const express = require("express");
const {
  globalErrorController,
  AppError,
} = require("@ark-industries/gogreen-common");
const fileRouter = require("./routes//filesRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/files", fileRouter);

app.use("*", (req, res, next) => {
  return next(new AppError(`Can't find path: ${req.path} on this server`, 404));
});

app.use(globalErrorController);

module.exports = app;
