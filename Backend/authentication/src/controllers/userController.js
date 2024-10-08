const pug = require("pug");
const User = require("../models/user");
const {
  AppError,
  catchAsync,
  JWT,
  Password,
  natsWrapper,
} = require("@ark-industries/gogreen-common");
const { devTransporter } = require("../util/email/nodemailer");
const EmailBuilder = require("../util/email/emailBuilder");
const { encryptToken } = require("../util/resetToken");
const UserCreatedPublisher = require("../events/userCreatedPublisher");
const UserUpdatedPublisher = require("../events/userUpdatedPublisher");
const UserDeletedPublisher = require("../events/userDeletedPublisher");
const UserForgetPasswordPublisher = require("../events/userForgetPasswordPublisher");
const UserResetPasswordPublisher = require("../events/userResetPasswordPublisher");

const fieldsToExclude = [
  "email",
  "password",
  "isActive",
  "invalidLoginCount",
  "userType",
  "phoneNo",
  "financeAllowed",
  "passwordChangedAt",
  "resetToken",
  "resetTokenExpireAt",
  "otp",
  "otpExpireAt",
  "blocked",
  "__v",
];

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

  if (user.blocked.isBlocked) {
    return next(
      new AppError(
        `Your account has been blocked by the team due to ${user.blocked.reason}. Please contact support for further assistance.`,
        401
      )
    );
  }

  const JWT_KEY = process.env.JWT_KEY;
  const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY) / (24 * 60 * 60) + "d"; // recieving 10 days in seconds converting to 10 day

  const Jwt = JWT.sign(
    {
      _id: user._id,
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
  const { name, email, password, userType, phoneNo, image } = req.body;

  const userObject = {
    name,
    email,
    password,
    userType,
    phoneNo,
  };

  if (image) {
    userObject.image = {
      id: image.id,
      url: image.url,
    };
  }

  const newUser = await User.create(userObject);

  await new UserCreatedPublisher(natsWrapper.client)
    .publish(newUser)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const JWT_KEY = process.env.JWT_KEY;
  const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY) / (24 * 60 * 60) + "d"; // recieving 10 days in seconds converting to 10 day

  const Jwt = JWT.sign(
    {
      _id: newUser._id,
      email: newUser.email,
    },
    JWT_KEY,
    JWT_EXPIRY
  );

  const fieldsToInclude = [
    "email",
    "isActive",
    "userType",
    "phoneNo",
    "image",
    "financeAllowed",
    "passwordChangedAt",
    "blocked",
  ];
  const fieldsToExclud = fieldsToExclude.filter(
    (field) => !fieldsToInclude.includes(field)
  );

  let userResult = await newUser.removeFields(fieldsToExclud);

  res.status(200).json({
    status: "success",
    data: { Jwt, user: userResult },
  });
});

const currentUser = catchAsync(async (req, res, next) => {
  const currentUser = req.currentUser;

  const user = await User.findById(currentUser._id);

  const fieldsToInclude = [
    "email",
    "isActive",
    "userType",
    "phoneNo",
    "image",
    "financeAllowed",
    "passwordChangedAt",
    "blocked",
  ];

  const fieldsToExclud = fieldsToExclude.filter(
    (field) => !fieldsToInclude.includes(field)
  );

  const userResult = await user.removeFields(fieldsToExclud);

  res.status(200).json({
    status: "success",
    data: { user: userResult },
  });
});

const getUsers = catchAsync(async (req, res, next) => {
  const currentUser = req.currentUser;

  const isAllowed = currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  let users = await User.find({});

  const fieldsToInclude = [
    "email",
    "isActive",
    "userType",
    "phoneNo",
    "image",
    "financeAllowed",
    "passwordChangedAt",
    "blocked",
  ];
  const fieldsToExclud = fieldsToExclude.filter(
    (field) => !fieldsToInclude.includes(field)
  );

  let usersResult = await Promise.all(
    users.map(async (user) => user.removeFields(fieldsToExclud))
  );

  res.status(200).json({
    status: "success",
    length: usersResult.length,
    data: { users: usersResult },
  });
});

const getUser = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  const user = await User.findById(userId);

  const isAllowed =
    req.currentUser.userType === "admin" ||
    user._id.toString() === req.currentUser._id.toString();

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  if (!user) {
    return next(new AppError(`No user find with the id: ${userId}.`, 404));
  }

  const fieldsToInclude = [
    "email",
    "isActive",
    "userType",
    "phoneNo",
    "image",
    "financeAllowed",
    "passwordChangedAt",
    "blocked",
  ];
  const fieldsToExclud = fieldsToExclude.filter(
    (field) => !fieldsToInclude.includes(field)
  );

  let userResult = await user.removeFields(fieldsToExclud);

  res.status(200).json({
    status: "success",
    data: { user: userResult },
  });
});

const updateUser = catchAsync(async (req, res, next) => {
  const userId = req.params.id;
  const { name, email, password, currentPassword, userType, phoneNo, image } =
    req.body;

  let user = await User.findById(userId);

  if (!user) {
    return next(new AppError(`No user find with the id: ${userId}.`, 404));
  }

  const isAllowed =
    req.currentUser._id.toString() === user._id.toString() ||
    req.currentUser.userType === "admin";

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

  if (password) {
    const oldPasswordMatched = await user.checkPassword(currentPassword);

    if (!oldPasswordMatched) {
      return next(new AppError("Current password not matched", 400));
    }

    const isNewPasswordSameAsOld = await Password.compare(
      user.password,
      password
    );

    if (isNewPasswordSameAsOld) {
      return next(new AppError("New password same as old", 409));
    }

    user.password = password;
  }

  user.phoneNo = phoneNo ? phoneNo : user.phoneNo;
  if (image) {
    user.image = { id: image.id, url: image.url } || user.image;
  }

  user = await user.save({ validateBeforeSave: false });

  await new UserUpdatedPublisher(natsWrapper.client).publish(user).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });

  const fieldsToInclude = [
    "email",
    "isActive",
    "userType",
    "phoneNo",
    "image",
    "financeAllowed",
    "passwordChangedAt",
    "blocked",
  ];

  const fieldsToExclud = fieldsToExclude.filter(
    (field) => !fieldsToInclude.includes(field)
  );

  let userResult = await user.removeFields(fieldsToExclud);

  res.status(200).json({
    status: "success",
    data: { user: userResult },
  });
});

const deleteUser = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  let user = await User.findById(userId);

  if (!user) {
    return next(new AppError(`No user find with the id: ${userId}.`, 404));
  }

  const isAllowed =
    req.currentUser._id.toString() === user._id.toString() ||
    req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  await user.updateOne({ isActive: false, userStatus: "Offline" });

  await new UserDeletedPublisher(natsWrapper.client)
    .publish({ id: user._id })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(204).json({
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
        resetToken,
      }
    );

    const emailUtl = new EmailBuilder()
      .setFrom(process.env.RESET_SUPPORT_EMAIL)
      .setTo(user.email)
      .setSubject("Password reset request for your account")
      .setHtml(html)
      .setTransporter(devTransporter())
      .build();

    console.log(process.env.RESET_SUPPORT_EMAIL);
    const info = await emailUtl.sendMail();

    await new UserForgetPasswordPublisher(natsWrapper.client)
      .publish({
        id: user._id,
        resetToken: user.resetToken,
        resetTokenExpireAt: user.resetTokenExpireAt,
      })
      .catch((err) => {
        console.log(err);
        return next(new AppError("Something went wrong...", 500));
      });

    res.status(200).json({
      status: "success",
      message: `Reset token is sent to ${info.accepted[0]}. The token is valid for 10 mintus.`,
    });
  } catch (err) {
    console.log(err);
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
  user.resetToken = null;
  user.resetTokenExpireAt = null;
  await user.save();

  await new UserResetPasswordPublisher(natsWrapper.client)
    .publish({
      id: user._id,
      newPassword: user.password,
      passwordChangedAt: user.passwordChangedAt,
    })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const JWT_KEY = process.env.JWT_KEY;
  const JWT_EXPIRY = parseInt(process.env.JWT_EXPIRY) / (24 * 60 * 60) + "d"; // recieving 10 days in seconds converting to 10 day

  const Jwt = JWT.sign(
    {
      _id: user._id,
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
