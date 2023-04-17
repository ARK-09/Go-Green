const AppError = require("../util/appError");
const restrictTo = (...roles) => {
  return (req, res, next) => {
    if (!req.currentUser) {
      return next(new AppError("You'r not loged in. Please login again.", 401));
    }
    if (!roles.includes(req.currentUser.userType)) {
      return next(
        new AppError("You don't have permission to perform this action.", 403)
      );
    }

    next();
  };
};

module.exports = restrictTo;
