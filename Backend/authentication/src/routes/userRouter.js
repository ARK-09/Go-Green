const express = require("express");
const { body } = require("express-validator");

const requireAuth = require("../middlewares/requireAuth");
const restrictTo = require("../middlewares/restrictTo");
const validateRequest = require("../middlewares/validateRequest");
const UserController = require("../controllers/userController");

const router = express.Router();

router
  .route("/login")
  .post(
    body("email")
      .isEmail()
      .normalizeEmail()
      .withMessage("Please provide a valid email address"),
    body("password").not().isEmpty().withMessage("Password can't be empty"),
    validateRequest,
    UserController.login
  );
router
  .route("/signup")
  .post(
    body("name")
      .trim()
      .notEmpty()
      .isLength({ min: 3 })
      .withMessage("Name should be at least 3 characters long"),
    validateRequest,
    UserController.signUp
  );

router.route("/currentuser").get(requireAuth, UserController.currentUser);

router
  .get("/", requireAuth, restrictTo("admin"), UserController.getUsers)
  .post("/forgetpassword", requireAuth, UserController.forgetPassword)
  .post(
    "/resetpassword/:resetToken",
    requireAuth,
    UserController.resetPassword
  );

router
  .route("/:id")
  .get(requireAuth, restrictTo("admin"), UserController.getUser)
  .patch(requireAuth, UserController.updateUser)
  .delete(requireAuth, UserController.deleteUser);

module.exports = router;
