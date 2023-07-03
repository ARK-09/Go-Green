const express = require("express");
const {
  globalErrorController,
  AppError,
} = require("@ark-industries/gogreen-common");
const profileRouter = require("./routers/profileRouter");
const projectRouter = require("./routers/projectRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/profiles", profileRouter);
app.use("/api/v1/profiles/:id/projects", projectRouter);

app.use("*", (req, res, next) => {
  return next(new AppError(`Can't find path: ${req.path} on this server`, 404));
});

app.use(globalErrorController);

module.exports = app;
