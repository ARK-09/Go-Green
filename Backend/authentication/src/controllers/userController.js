const User = require("../models/user");
const AppError = require("../util/appError");
const catchAsync = require("../util/catchAsync");
const Jwt = require("../util/jwt");

const login = catchAsync(async (req, res, next) => {
  const { email, password } = req.body;
  const user = await User.findOne({ email });

  if (!user) {
    return next(new AppError("Invalid email or password", 400));
  }

  if (!user.isActive) {
    return next(
      new AppError(
        "Your account has been deleted. Please contact support for further assistance.",
        401
      )
    );
  }

  const passwordMatch = await user.checkPassword(password);

  if (!passwordMatch) {
    return next(new AppError("Invalid email or password", 400));
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

const currentUser = catchAsync(async (req, res, next) => {
  const currentUser = req.currentUser;

  const user = await User.findById(currentUser.id);

  res.status(200).json({
    status: "success",
    data: { user },
  });
});

const getUsers = catchAsync(async (req, res, next) => {
  const users = await User.find({});

  res.status(200).json({
    status: "success",
    length: users.length,
    data: { users },
  });
});

const getUser = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  const user = await User.findById(userId);

  if (!user) {
    return next(new AppError(`No user find with the id: ${userId}.`, 404));
  }

  res.status(200).json({
    status: "success",
    data: { user },
  });
});

const updateUser = catchAsync(async (req, res, next) => {
  const userId = req.params.id;
  const { name, email, password, phoneNo, image } = req.body;

  let user = await User.findById(userId);

  if (!user) {
    return next(new AppError(`No user find with the id: ${userId}.`, 404));
  }

  const isAllowed =
    req.currentUser.id === user.id || req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  user.name = name ? name : user.name;
  user.email = email ? email : user.email;
  user.password = password ? password : user.password;
  user.phoneNo = phoneNo ? phoneNo : user.phoneNo;
  user.image = image ? image : user.image;

  user = await user.save();

  res.status(200).json({
    status: "success",
    data: { user },
  });
});

const deleteUser = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  let user = await User.findById(userId);

  if (!user) {
    return next(new AppError(`No user find with the id: ${userId}.`, 404));
  }

  const isAllowed =
    req.currentUser.id === user.id || req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  await user.update({ isActive: false, userStatus: "Offline" });

  res.status(404).json({
    status: "success",
    data: { user: null },
  });
});

const forgetPassword = catchAsync(async (req, res, next) => {
  const { email } = req.body;
  const user = await User.findOne({ email });

  if (!user) {
    return next(new AppError("User with this email does not exist.", 404));
  }
});

exports.login = login;
exports.signUp = signUp;
exports.currentUser = currentUser;
exports.getUsers = getUsers;
exports.getUser = getUser;
exports.updateUser = updateUser;
exports.deleteUser = deleteUser;
exports.forgetPassword = forgetPassword;
