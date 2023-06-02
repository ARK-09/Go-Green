const express = require("express");
const currentUser = require("../middelwares/currentUser");
const { requireAuth, restrictTo } = require("@ark-industries/gogreen-common");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router();

router.route("/");

module.exports = router;
