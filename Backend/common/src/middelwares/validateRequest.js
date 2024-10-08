const { validationResult } = require("express-validator");
const AppError = require("../util/appError");

const validateRequest = async (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    const message = errors
      .array()
      .map((err) => `${err.param}: ${err.msg}`)
      .join(", ");
    const error = new AppError(message, 400);
    next(error);
  }
  next();
};

module.exports = validateRequest;
