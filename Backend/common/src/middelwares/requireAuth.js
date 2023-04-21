const AppError = require("../util/appError");
const catchAsync = require("../util/catchAsync");
const Jwt = require("../util/jwt");

const requireAuth = (JWT_KEY) => {
  return catchAsync(async (req, res, next) => {
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

    const payload = Jwt.verify(token, JWT_KEY);

    if (!payload) {
      return next(
        new AppError("Login token is invalid or has been expired.", 401)
      );
    }

    req.payload = payload;
    next();
  });
};

module.exports = requireAuth;
