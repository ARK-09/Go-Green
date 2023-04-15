const User = require("../models/user");
const AppError = require("../util/appError");
const catchAsync = require("../util/catchAsync");
const Jwt = require("../util/jwt");

module.exports = catchAsync(async (req, res, next) => {
  const headerAuthorization = req.headers.authorization;
  let token;

  if (headerAuthorization && headerAuthorization.startsWith("Bearer")) {
    token = headerAuthorization.split(" ")[1];
  } else if (req.cookies && req.cookies.JWT) {
    token = req.cookies.JWT;
  }

  if (!token) {
    return next(new AppError("Authorization header is missing.", 401));
  }

  const payload = Jwt.verify(token);

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

  req.currentUser = user;
  next();
});
