const express = require("express");
const AppError = require("./util/appError");
const globalErrorController = require("./controllers/globalErrorController");
const userRouter = require("./routes/userRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/users", userRouter);

app.use("*", (req, res, next) => {
  next(new AppError(`Can't find path: ${req.path} on this server`, 404));
});

app.use(globalErrorController);

module.exports = app;
