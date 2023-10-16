const { AppError } = require("@ark-industries/gogreen-common");
const catchAsyncSocketError = require("./catchAsyncSocketError");

const validatePayload = catchAsyncSocketError(async (data) => {
  if (typeof data != "object") {
    const error = new AppError("Event data should be a valid Json.");
    error.name = "JsonParseError";

    throw error;
  }
});

exports.validatePayload = validatePayload;
