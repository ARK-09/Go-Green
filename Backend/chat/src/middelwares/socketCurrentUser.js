const { AppError } = require("@ark-industries/gogreen-common");
const catchAsyncSocketError = require("../socket/util/catchAsyncSocketError");
const User = require("../models/user");

const socketCurrentUser = catchAsyncSocketError(async (socket, next) => {
  const user = await User.findById(socket.payload.id);

  if (!user) {
    return next(new AppError("The user with this token no longer exist", 204));
  }

  if (!user.isActive) {
    return next(
      new AppError(
        "Your account has been deleted. Please contact support for further assistance.",
        401
      )
    );
  }

  const userChagesPassword = await user.changesPasswordAfter(
    socket.payload.iat
  );

  if (userChagesPassword) {
    return next(
      new AppError(
        "You have recently changed your password. Please log in again",
        401
      )
    );
  }

  next();
});

module.exports = socketCurrentUser;
