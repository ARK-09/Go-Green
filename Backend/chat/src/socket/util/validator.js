const { AppError } = require("@ark-industries/gogreen-common");
const { isMongoId } = require("validator");
const SocketServer = require("../socketServer");
const catchAsyncSocketError = require("./catchAsyncSocketError");

const validatePayload = catchAsyncSocketError(async () => {
  const payload = SocketServer.getSocket().data;

  if (typeof payload != "object") {
    const error = new AppError("Event data should be a valid Json.");
    error.name = "JsonParseError";

    throw error;
  }

  SocketServer.getSocket().data = payload;
});

exports.validatePayload = validatePayload;
