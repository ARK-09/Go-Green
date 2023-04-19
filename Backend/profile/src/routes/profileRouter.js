const express = require("express");
const ProfileController = require("../controllers/profileController");

const router = express.Router();

router
  .route("/users/:id")
  .get(ProfileController.getUserProfile)
  .patch(ProfileController.updateUserProfile)
  .delete(ProfileController.deleteUserProfile);

module.exports = router;
