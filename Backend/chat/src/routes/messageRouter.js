const express = require("express");
const { sanitizeBody, check, param } = require("express-validator");
const MessageController = require("../controllers/messageController");
const {
  requireAuth,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const currentUser = require("../middelwares/currentUser");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router();

router.patch(
  "/chats/messages/:id",
  param("id")
    .isMongoId()
    .withMessage("Invalid message ID. Please provide a valid MongoDB ID."),
  check("text")
    .isString()
    .withMessage("Text should be of type string.")
    .trim()
    .notEmpty()
    .withMessage("Text field is required."),
  sanitizeBody("*").escape(),
  validateRequest,
  requireAuth(JWT_KEY),
  currentUser,
  MessageController.updateMessage
);

router.delete(
  "/chats/messages/:id",
  param("id")
    .isMongoId()
    .withMessage("Invalid message ID. Please provide a valid MongoDB ID."),
  validateRequest,
  requireAuth(JWT_KEY),
  currentUser,
  MessageController.deleteMessage
);

module.exports = router;
