const { AppError } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

const socketCurrentUser = async (socket, next) => {
  const user = await User.findById(socket.currentUser._id.toString());

  if (!user) {
    return next(new AppError("The user with this token no longer exist", 404));
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
    socket.currentUser.iat
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
};

module.exports = socketCurrentUser;
