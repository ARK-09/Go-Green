const { catchAsync } = require("@ark-industries/gogreen-common");
const User = require("../models/user");

const verifyUser = catchAsync(async (req, res, next) => {
  const payload = req.payload;

  const user = await User.findById(payload.id);

  if (!user) {
    return next(new AppError("The user with this token no longer exist", 401));
  }

  const userChagesPassword = await user.changesPasswordAfter(payload.iat);

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

module.exports = catchAsync;
