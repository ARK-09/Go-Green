const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

const currentUser = catchAsync(async (req, res, next) => {
  const user = await User.findById(req.payload.id);

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

  const userChagesPassword = await user.changesPasswordAfter(req.payload.iat);

  if (userChagesPassword) {
    return next(
      new AppError(
        "You have recently changed your password. Please log in again",
        401
      )
    );
  }

  req.currentUser = user;
  next();
});

module.exports = currentUser;
