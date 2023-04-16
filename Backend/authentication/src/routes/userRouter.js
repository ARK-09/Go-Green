const express = require("express");
const { body, check, param } = require("express-validator");

const requireAuth = require("../middlewares/requireAuth");
const restrictTo = require("../middlewares/restrictTo");
const validateRequest = require("../middlewares/validateRequest");
const UserController = require("../controllers/userController");
const AppError = require("../util/appError");

const router = express.Router({ mergeParams: true });

router
  .get("/", requireAuth, restrictTo("admin"), UserController.getUsers)
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
      .isIn(["talent", "client", "admin"])
      .withMessage(
        "Invalid user type. Allowed values are: 'talent', 'client'."
      ),
    check("phoneNo")
      .isMobilePhone("en-PK")
      .withMessage("Please provide a valid Pakistani mobile number."),
    validateRequest,
    UserController.signUp
  )
  .get("/currentuser", requireAuth, UserController.currentUser)
  .post(
    "/forgetpassword",
    check("email")
      .isString()
      .withMessage("Email should be of type string.")
      .isEmail()
      .withMessage("Please provide a valid email address")
      .normalizeEmail(),
    requireAuth,
    UserController.forgetPassword
  )
  .post(
    "/resetpassword/:resetToken",
    body("password").custom(async (input, { next }) => {
      const reg =
        /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&()])[A-Za-z\d@$!%*#?&()]{8,}$/;
      const matches = new RegExp(reg).test(input);
      if (!matches) {
        throw (new Error(
          "The password should be at least 8 characters long and contain a combination of alphanumeric characters and at least one special character [@, $, !, %, *, #, ?, &, (, )]."
        ).status = 400);
      }
    }),
    requireAuth,
    UserController.resetPassword
  );

router
  .route("/:id")
  .get(
    param("id").isMongoId().withMessage("Please provide a valid id."),
    requireAuth,
    restrictTo("admin"),
    UserController.getUser
  )
  .patch(requireAuth, UserController.updateUser)
  .delete(requireAuth, UserController.deleteUser);

module.exports = router;
