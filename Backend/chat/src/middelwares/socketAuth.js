const { AppError, JWT } = require("@ark-industries/gogreen-common");
const catchAsyncSocketError = require("../socket/util/catchAsyncSocketError");

const JWT_KEY = process.env.JWT_KEY;

const socketAuth = catchAsyncSocketError(async (socket, next) => {
  const headerAuthorization = socket.handshake.headers.authorization;
  let token;

  if (headerAuthorization && headerAuthorization.startsWith("Bearer")) {
    token = headerAuthorization.split(" ")[1];
  }

  if (!token) {
    return next(new AppError("Authorization header is missing.", 401));
  }

  const payload = JWT.verify(token, JWT_KEY);
  if (!payload) {
    return next(
      new AppError("Login token is invalid or has been expired.", 401)
    );
  }

  socket.payload = payload;
  next();
});

module.exports = socketAuth;
