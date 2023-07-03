const express = require("express");
const ProfileController = require("../controllers/profileController");
const currentUser = require("../middelwares/currentUser");
const {
  requireAuth,
  restrictTo,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const { check, param } = require("express-validator");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router();

router
  .route("/users/:id")
  .get(
    param("id").isMongoId().withMessage("Invalid user ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProfileController.getUserProfile
  )
  .patch(
    param("id").isMongoId().withMessage("Invalid user ID."),
    check("about")
      .optional()
      .isString()
      .withMessage("About should be a string.")
      .escape(),
    check("languages")
      .optional()
      .isArray()
      .withMessage("Languages should be an array.")
      .bail()
      .custom((value) => {
        // Check each element in the languages array
        if (Array.isArray(value)) {
          for (const language of value) {
            if (
              typeof language !== "object" ||
              !language.name ||
              !language.experience
            ) {
              throw new Error("Invalid language object.");
            }
          }
        }
        return true;
      })
      .withMessage("Invalid language object."),
    check("languages.*.name")
      .isString()
      .withMessage("Language name should be a string."),
    check("languages.*.experience")
      .isString()
      .withMessage("Language experience should be a string.")
      .isIn(["Beginner", "Intermediate", "Fluent", "Native speaker"])
      .withMessage(
        'Language experience can have only these values ["Beginner", "Intermediate", "Fluent", "Native speaker"].'
      ),
    check("dob")
      .optional()
      .isISO8601()
      .toDate()
      .withMessage(
        "Invalid date of birth. Please provide a valid date in ISO 8601 format."
      ),
    check("address")
      .optional()
      .isString()
      .withMessage("Address should be a string."),
    check("location")
      .optional()
      .isObject()
      .withMessage("Location should be an object.")
      .bail()
      .custom((value, { req }) => {
        if (
          !value ||
          (value && !value.latitude) ||
          (value && !value.longitude)
        ) {
          throw new Error("Invalid location object.");
        }
        return true;
      })
      .withMessage("Location should contain latitude and longitude."),
    check("skills")
      .optional()
      .isArray()
      .withMessage("Skills should be an array."),
    check("skills.*")
      .optional()
      .isMongoId()
      .withMessage("Skills should be an array with valid id's."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProfileController.updateUserProfile
  )
  .delete(
    param("id").isMongoId().withMessage("Invalid user ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    ProfileController.deleteUserProfile
  );

module.exports = router;
