const express = require("express");
const {
  globalErrorController,
  AppError,
} = require("@ark-industries/gogreen-common");

const jobsRouter = require("./routers/jobsRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/jobs", jobsRouter);

app.use("*", (req, res, next) => {
  next(new AppError(`Can't find path: ${req.path} on this server`, 404));
});

app.use(globalErrorController);

module.exports = app;
