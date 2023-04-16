const express = require("express");
const ProfileController = require("../controllers/profileController");

const router = express.Router();

router.get("/users/:id", ProfileController.getUserProfile);

module.exports = router;
