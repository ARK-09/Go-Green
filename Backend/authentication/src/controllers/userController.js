const User = require("../models/user");
const AppError = require("../util/appError");
const catchAsync = require("../util/catchAsync");
const Password = require("../util/password");
const Jwt = require("../util/jwt");

const login = catchAsync(async (req, res, next) => {
  const { email, password } = req.body;
  const user = await User.findOne({ email });

  if (!user) {
    return next(new AppError(`Invalid email or password`, 400));
  }

  const passwordMatch = await Password.compare(user.password, password);

  if (!passwordMatch) {
    return next(new AppError(`Invalid email or password`, 400));
  }

  const JWT = Jwt.sign({
    id: user.id,
    email: user.email,
  });

  res.status(200).json({
    status: "success",
    data: { JWT },
  });
});

const signUp = catchAsync(async (req, res, next) => {
  const { name, email, password, userType, phoneNo, image } = req.body;

  const newUser = await User.create({
    name,
    email,
    password,
    userType,
    phoneNo,
    image,
  });

  const JWT = Jwt.sign({
    id: newUser.id,
    email: newUser.email,
  });

  res.status(200).json({
    status: "success",
    data: { JWT, user: newUser },
  });
});

exports.login = login;
exports.signUp = signUp;
