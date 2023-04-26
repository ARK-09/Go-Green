const pug = require("pug");
const User = require("../models/user");
const {
  AppError,
  catchAsync,
  JWT,
  natsWrapper,
} = require("@ark-industries/gogreen-common");
const { devTransporter } = require("../util/email/nodemailer");
const EmailBuilder = require("../util/email/emailBuilder");
const { encryptToken } = require("../util/resetToken");
const UserCreatedPublisher = require("../events/userCreatedPublisher");

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

  const JWT_KEY = process.env.JWT_KEY;
  const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY) / (24 * 60 * 60) + "d"; // recieving 10 days in seconds converting to 10 day

  const Jwt = JWT.sign(
    {
      id: user.id,
      email: user.email,
    },
    JWT_KEY,
    JWT_EXPIRY
  );

  res.status(200).json({
    status: "success",
    data: { Jwt },
  });
});

const signUp = catchAsync(async (req, res, next) => {
  const { name, email, password, userType, phoneNo } = req.body;

  const newUser = await User.create({
    name,
    email,
    password,
    userType,
    phoneNo,
    image: "",
  });

  await new UserCreatedPublisher(natsWrapper.client).publish(newUser);

  const JWT_KEY = process.env.JWT_KEY;
  const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY) / (24 * 60 * 60) + "d"; // recieving 10 days in seconds converting to 10 day

  const Jwt = JWT.sign(
    {
      id: newUser.id,
      email: newUser.email,
    },
    JWT_KEY,
    JWT_EXPIRY
  );

  res.status(200).json({
    status: "success",
    data: { Jwt, user: newUser },
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
  const { name, email, password, userType, phoneNo, image } = req.body;

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

  if (req.currentUser.userType === "admin") {
    user.userType = userType ? userType : user.userType;
  }

  user.name = name ? name : user.name;
  user.email = email ? email : user.email;
  user.password = password ? password : user.password;
  user.phoneNo = phoneNo ? phoneNo : user.phoneNo;

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

  await user.updateOne({ isActive: false, userStatus: "Offline" });

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

  const resetToken = await user.createResetToken();
  await user.save({ validateBeforeSave: false });

  try {
    const resetUrl = `${req.protocol}://${req.get(
      "host"
    )}/api/v1/users/resetpassword/${resetToken}`;

    const html = pug.renderFile(
      `${__dirname}/../views/email/resetPassword.pug`,
      {
        userName: user.name,
        resetUrl,
      }
    );

    const emailUtl = new EmailBuilder()
      .setFrom("info@gogreen.com")
      .setTo(user.email)
      .setSubject("Password reset request for your account")
      .setHtml(html)
      .setTransporter(devTransporter())
      .build();

    const info = await emailUtl.sendMail();
    res.status(200).json({
      status: "success",
      message: `Reset token is sent to ${info.accepted[0]}. The token is valid for 10 mintus.`,
    });
  } catch (err) {
    user.resetToken = undefined;
    user.resetTokenExpireAt = undefined;
    await user.save({ validateBeforeSave: false });

    return next(
      new AppError(
        "There was an error sending the email. Try again later!",
        500
      )
    );
  }
});

const resetPassword = catchAsync(async (req, res, next) => {
  const { resetToken } = req.params;
  const { password } = req.body;

  const hashedToken = encryptToken(resetToken);

  const user = await User.findOne({
    resetToken: hashedToken,
    resetTokenExpireAt: { $gt: Date.now() },
  });

  if (!user) {
    return next(new AppError("Token is invalid or has expired", 400));
  }

  user.password = password;
  user.resetToken = undefined;
  user.resetTokenExpireAt = undefined;
  await user.save();

  const JWT_KEY = process.env.JWT_KEY;
  const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY) / (24 * 60 * 60) + "d"; // recieving 10 days in seconds converting to 10 day

  const Jwt = JWT.sign(
    {
      id: user.id,
      email: user.email,
    },
    JWT_KEY,
    JWT_EXPIRY
  );

  res.status(200).json({
    status: "success",
    data: { Jwt },
  });
});

exports.login = login;
exports.signUp = signUp;
exports.currentUser = currentUser;
exports.getUsers = getUsers;
exports.getUser = getUser;
exports.updateUser = updateUser;
exports.deleteUser = deleteUser;
exports.forgetPassword = forgetPassword;
exports.resetPassword = resetPassword;
