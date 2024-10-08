const mongoose = require("mongoose");
const validator = require("validator");
const { Password } = require("@ark-industries/gogreen-common");

const userSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, "Please provide a name"],
    minlength: [3, "Name must be at least 3 characters long"],
    maxlength: [25, "Name cannot be more than 25 characters long"],
    trim: true,
  },
  email: {
    type: String,
    required: [true, "Please provide an email"],
    unique: true,
    lowercase: true,
    trim: true,
    validate: {
      validator: validator.isEmail,
      message: "Please provide a valid email address",
    },
  },
  password: {
    type: String,
    required: [true, "Please provide a password"],
    validate: {
      validator: function (value) {
        const reg =
          /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&()])[A-Za-z\d@$!%*#?&()]{8,}$/;
        const regex = new RegExp(reg);
        return regex.test(value);
      },
      message:
        "The password should be at least 8 characters long and contain a combination of alphanumeric characters and at least one special character [@, $, !, %, *, #, ?, &, (, )].",
    },
  },
  passwordChangedAt: {
    type: Date,
    default: Date.now,
  },
  resetToken: {
    type: String,
    default: null,
  },
  resetTokenExpireAt: {
    type: Date,
    default: null,
  },
  otp: {
    type: Number,
    min: [1000, "OTP must be a 4-digit number"],
    max: [9999, "OTP must be a 4-digit number"],
    default: null,
  },
  otpExpireAt: {
    type: Date,
    default: null,
  },
  isActive: {
    type: Boolean,
    default: true,
  },
  invalidLoginCount: {
    type: Number,
    max: [5, "Maximum invalid login count is 5"],
    default: 0,
  },
  userType: {
    type: String,
    enum: {
      values: ["talent", "client", "admin"],
      message: "Invalid user type.",
    },
    required: [true, "Please provide a user type"],
    default: "client",
  },
  phoneNo: {
    type: String,
    required: [true, "Please provide a phone number"],
    trim: true,
    validator: {
      validate: (value) => {
        return validator.isMobilePhone(value, ["PK"]);
      },
      message:
        "Please provide a valid mobile number. Note only Paksitan number are supported.",
    },
  },
  image: {
    type: String,
    default: null,
  },
  userStatus: {
    type: String,
    enum: ["Online", "Offline"],
    default: "Online",
    trim: true,
  },
  verified: {
    type: Boolean,
    default: false,
  },
  joinedDate: {
    type: Date,
    default: Date.now,
  },
  financeAllowed: {
    type: Boolean,
    default: true,
  },
  blocked: {
    isBlocked: {
      type: Boolean,
      default: false,
    },
    reason: {
      type: String,
      default: null,
      trim: true,
    },
  },
});

userSchema.pre("save", async function (next) {
  if (this.isModified("password")) {
    this.password = await Password.toHash(this.password);
    this.passwordChangedAt = Date.now() - 60 * 1000;
  }
  next();
});

userSchema.methods.checkPassword = async function (password) {
  return await Password.compare(this.password, password);
};

userSchema.methods.changesPasswordAfter = async function (JWTTimestemp) {
  if (this.passwordChangedAt) {
    const changedTimestamp = parseInt(
      this.passwordChangedAt.getTime() / 1000,
      10
    );
    return JWTTimestemp < changedTimestamp;
  }

  // False means NOT changed
  return false;
};

const User = mongoose.model("User", userSchema);

module.exports = User;
