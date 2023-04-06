const User = require("../models/user");
const AppError = require("../util/appError");
const catchAsync = require("../util/catchAsync");
const Password = require("../util/password");
const jwt = require("jsonwebtoken");

const login = catchAsync(async (req, res, next) => {
    const { email, password } = req.body;
    const user = User.find({ email })

    if (!user) {
        return next(new AppError(`Invalid email or password`, 400));
    }

    const passwordMatch = await Password.compare(user.password, password);

    if (!passwordMatch) {
        return next(new AppError(`Invalid email or password`, 400));
    }

    const JWT = jwt.sign({
        id: user.id,
        email: user.email
    }, process.env.JWT_KEY);

    res.status(200).json({
        status: "success",
        data: { JWT }
    });
})

exports.login = login;