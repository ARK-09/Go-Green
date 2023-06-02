const express = require("express");
const ProfileController = require("../controllers/profileController");
const currentUser = require("../middelwares/currentUser");
const { requireAuth, restrictTo } = require("@ark-industries/gogreen-common");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router();

router
  .route("/users/:id")
  .get(requireAuth(JWT_KEY), currentUser, ProfileController.getUserProfile)
  .patch(requireAuth(JWT_KEY), currentUser, ProfileController.updateUserProfile)
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    ProfileController.deleteUserProfile
  );

module.exports = router;
