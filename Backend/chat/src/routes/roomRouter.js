const express = require("express");
const { sanitizeBody, check, param } = require("express-validator");
const RoomController = require("../controllers/roomController");
const {
  requireAuth,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const currentUser = require("../middelwares/currentUser");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

router
  .route("/")
  .post(
    check("name")
      .isString()
      .withMessage("Name should be of type string.")
      .trim()
      .notEmpty()
      .withMessage("Name field is required."),
    check("members")
      .isArray({ min: 1 })
      .withMessage(
        "Members should be provided as an array with at least one user ID."
      ),
    check("members.*")
      .isMongoId()
      .withMessage("Invalid member ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.createRoom
  )
  .get(
    query("offset")
      .optional()
      .isInt()
      .withMessage("Offset should be an integer."),
    query("limit")
      .optional()
      .isInt()
      .withMessage("Limit should be an integer."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.getRooms
  );

router
  .route("/:id")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid room ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.getRoomById
  );

router
  .route("/:id/messages")
  .post(
    param("id")
      .isMongoId()
      .withMessage("Invalid room ID. Please provide a valid MongoDB ID."),
    check("text")
      .isString()
      .withMessage("Text should be of type string.")
      .trim()
      .notEmpty()
      .withMessage("Text field is required."),
    check("attachments.mimeType")
      .optional()
      .isString()
      .withMessage("Attachments mimeType should be of type string."),
    check("attachments.file")
      .optional()
      .isObject()
      .withMessage("Attachments file should be provided as an object."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.createRoomMessage
  )
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid room ID. Please provide a valid MongoDB ID."),
    query("offset")
      .optional()
      .isInt()
      .withMessage("Offset should be an integer."),
    query("limit")
      .optional()
      .isInt()
      .withMessage("Limit should be an integer."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.getRoomMessages
  );

router
  .route("/:id/members")
  .post(
    param("id")
      .isMongoId()
      .withMessage("Invalid room ID. Please provide a valid MongoDB ID."),
    check("members")
      .isArray({ min: 1 })
      .withMessage(
        "Members should be provided as an array with at least one user ID."
      ),
    check("members.*")
      .isMongoId()
      .withMessage("Invalid member ID. Please provide a valid MongoDB ID."),
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.addRoomMembers
  )
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid room ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.getRoomMembers
  );

router
  .route("/:id/members/:memberid")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid room ID. Please provide a valid MongoDB ID."),
    param("memberId")
      .isMongoId()
      .withMessage("Invalid member ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.getRoomMember
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid room ID. Please provide a valid MongoDB ID."),
    param("memberId")
      .isMongoId()
      .withMessage("Invalid member ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    RoomController.deleteRoomMember
  );

module.exports = router;
