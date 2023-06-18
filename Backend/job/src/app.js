const express = require("express");
const {
  globalErrorController,
  AppError,
} = require("@ark-industries/gogreen-common");

const contractRouter = require("./routes/contractsRouter");
const jobRouter = require("./routes/jobsRouter");
const proposalRouter = require("./routes/proposalsRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/contracts", contractRouter);
app.use("/api/v1/jobs", jobRouter);
app.use("/api/v1/proposals", proposalRouter);

app.use("*", (req, res, next) => {
  next(new AppError(`Can't find path: ${req.path} on this server`, 404));
});

app.use(globalErrorController);

module.exports = app;
