const express = require("express");
const { body, check, param, sanitizeBody } = require("express-validator");
const currentUser = require("../middlewares/currentUser");
const {
  requireAuth,
  restrictTo,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const UserController = require("../controllers/userController");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router();

router
  .get(
    "/",
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    UserController.getUsers
  )
  .post(
    "/login",
    check("email")
      .isString()
      .withMessage("Email should be of type string.")
      .isEmail()
      .withMessage("Please provide a valid email address")
      .normalizeEmail(),
    check("password")
      .isString()
      .withMessage("Password should be of type string.")
      .not()
      .notEmpty()
      .withMessage("Password can't be empty")
      .escape(),
    validateRequest,
    UserController.login
  )
  .post(
    "/signup",
    check("name")
      .isString()
      .withMessage("Name should be of type string.")
      .matches(/^[^!@#$%^&*()_+=,.;\/\\{}[\]`'"?><]+$/)
      .withMessage(
        "Please note that the following characters are not allowed: !@#$%^&*()_+=,.;/{}[]`''\"><?"
      )
      .trim()
      .isLength({ min: 3, max: undefined })
      .withMessage("Name should be at least 3 characters long")
      .escape(),
    check("email")
      .isString()
      .withMessage("Email should be of type string.")
      .isEmail()
      .withMessage("Please provide a valid email address")
      .normalizeEmail(),
    check("password")
      .isString()
      .withMessage("Password should be of type string.")
      .not()
      .notEmpty()
      .withMessage("Password can't be empty")
      .escape(),
    check("userType")
      .notEmpty()
      .withMessage("user type can't be empty.")
      .isIn(["talent", "client"])
      .withMessage(
        "Invalid user type. Allowed values are: 'talent', 'client'."
      ),
    check("phoneNo")
      .isMobilePhone("en-PK")
      .withMessage("Please provide a valid Pakistani mobile number."),
    validateRequest,
    UserController.signUp
  )
  .get(
    "/currentuser",
    requireAuth(JWT_KEY),
    currentUser,
    UserController.currentUser
  )
  .post(
    "/forgetpassword",
    check("email")
      .isString()
      .withMessage("Email should be of type string.")
      .isEmail()
      .withMessage("Please provide a valid email address")
      .normalizeEmail(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    UserController.forgetPassword
  )
  .post(
    "/resetpassword/:resetToken",
    body("password")
      .matches(
        /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&()])[A-Za-z\d@$!%*#?&()]{8,}$/
      )
      .withMessage(
        "The password should be at least 8 characters long and contain a combination of alphanumeric characters and at least one special character [@, $, !, %, *, #, ?, &, (, )]."
      ),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    UserController.resetPassword
  );

router
  .route("/:id")
  .get(
    param("id").isMongoId().withMessage("Please provide a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    UserController.getUser
  )
  .patch(
    param("id")
      .isMongoId()
      .withMessage("Invalid user ID. Please provide a valid MongoDB ID."),
    check("name")
      .isString()
      .withMessage("Name should be of type string.")
      .trim()
      .notEmpty()
      .withMessage("Name field is required."),
    check("email")
      .isEmail()
      .withMessage("Please provide a valid email address.")
      .normalizeEmail(),
    check("password")
      .matches(
        /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&()])[A-Za-z\d@$!%*#?&()]{8,}$/
      )
      .withMessage(
        "Password must contain at least 8 characters, one letter, one number, and one special character."
      )
      .notEmpty()
      .withMessage("Password field is required."),
    check("phoneNo")
      .isMobilePhone()
      .withMessage("Please provide a valid phone number.")
      .optional(),
    check("image")
      .isURL()
      .withMessage("Please provide a valid image URL.")
      .optional(),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    UserController.updateUser
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid user ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    UserController.deleteUser
  );

module.exports = router;
