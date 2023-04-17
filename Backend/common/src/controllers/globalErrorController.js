const AppError = require("../util/appError");

const handelProductionErrors = (err, res) => {
  if (!err.isOperational) {
    err.status = "error";
    err.statusCode = 500;
    err.message = "Something went wrong...";
  }

  res.status(err.statusCode).json({
    status: err.status,
    code: err.statusCode,
    message: err.message,
  });
};

const handelDevelopmentErrors = (err, res) => {
  res.status(err.statusCode).json({
    status: err.status,
    statusCode: err.statusCode,
    message: err.message,
    stack: err.stack,
    error: err,
  });
};

const handelCastErrors = (err) => {
  const message = `Invalid ${err.path}: ${err.value}`;
  return new AppError(message, 400);
};

const handelDuplicateFieldErrors = (err) => {
  const value = err.message.match(/(["'])(\\?.)*?\1/)[0];
  const message = err.keyPattern?.email
    ? "This email is already in use."
    : `Duplicate field value: ${value}. Please use another value!`;
  return new AppError(message, 400);
};

const handelValidateErrorDB = (err) => {
  const errors = Object.values(err.errors).map((el) => el.message);

  const message = `Invalid input data. ${errors.join(". ")}`;
  return new AppError(message, 400);
};

const handelJsonWebTokenError = () => {
  return new AppError("Invalid token. Please log in again", 401);
};

const handelTokenExpiredError = () => {
  return new AppError("You'r token has been expired. Please log in again", 401);
};

const globalErrorController = async (err, req, res, next) => {
  err.statusCode = err.statusCode || 500;
  err.status = err.status || "error";

  if (process.env.NODE_ENV === "development") {
    handelDevelopmentErrors(err, res);
  } else if (process.env.NODE_ENV === "production") {
    let error = { ...err, message: err.message };

    if (err.name === "CastError") {
      error = handelCastErrors(error);
    }

    if (err.code === 11000) {
      error = handelDuplicateFieldErrors(err);
    }

    if (err.name === "ValidationError") {
      error = handelValidateErrorDB(error);
    }

    if (err.name === "JsonWebTokenError") {
      error = handelJsonWebTokenError();
    }

    if (err.name === "TokenExpiredError") {
      error = handelTokenExpiredError();
    }

    handelProductionErrors(error, res);
  }
};

module.exports = globalErrorController;
