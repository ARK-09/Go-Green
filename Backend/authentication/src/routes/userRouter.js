const express = require("express");
const UserController = require("../controllers/userController");

const router = express.Router();

router.route("/login").post(UserController.login);
router.route("/signup").post(UserController.signUp);

module.exports = router;
