const express = require("express");
const mongoose = require("mongoose");
const ProfileController = require("../controllers/profileController");
const currentUser = require("../middelwares/currentUser");
const {
  requireAuth,
  restrictTo,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const { check, param, query } = require("express-validator");

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
      .custom((location) => {
        const isLatitude = (num) => isFinite(num) && Math.abs(num) <= 90;
        const isLongitude = (num) => isFinite(num) && Math.abs(num) <= 180;

        if (!Array.isArray(location.coordinates)) {
          throw new Error(
            "Location coordinates should contain exactly two elements: [longitude, latitude]."
          );
        }

        if (
          location.coordinates.some((coord) => {
            const cord = parseFloat(coord);
            typeof cord !== "number" || isNaN(cord) || !isFinite(cord);
          })
        ) {
          throw new Error("Coordinates should be valid float numbers.");
        }

        const [longitude, latitude] = location.coordinates;

        if (!isLatitude(latitude)) {
          throw new Error("Latitude must be a number between -90 and 90.");
        }

        if (!isLongitude(longitude)) {
          throw new Error("Longitude must be a number between -180 and 180.");
        }
        return true;
      }),
    check("skills")
      .optional()
      .isArray({ min: 1 })
      .withMessage("Skills should be an array."),
    check("skills.*")
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

router.route("/search").get(
  query("name")
    .optional()
    .isString()
    .trim()
    .notEmpty()
    .withMessage("Name must be a non-empty string.")
    .escape(),
  query("skills")
    .optional()
    .isArray({ min: 1 })
    .withMessage(
      "Skills should be provided as an array with at least one element."
    ),
  query("skills.*").isString().withMessage("Skills should be a string."),
  query("skills").customSanitizer((value) => {
    if (Array.isArray(value)) {
      return value.map((title) =>
        title
          .replace(/&/g, "&amp;")
          .replace(/</g, "&lt;")
          .replace(/>/g, "&gt;")
          .replace(/"/g, "&quot;")
          .replace(/'/g, "&#x27;")
          .replace(/`/g, "&#x60;")
      );
    }
  }),
  query("location")
    .optional()
    .isObject()
    .withMessage("Location should be an object.")
    .bail()
    .if(query("location.coordinates").exists())
    .isArray({ min: 2, max: 2 })
    .withMessage(
      "Coordinates should be an array containing exactly two elements: [longitude, latitude]."
    )
    .bail()
    .custom((coordinates) => {
      const isLatitude = (num) => isFinite(num) && Math.abs(num) <= 90;
      const isLongitude = (num) => isFinite(num) && Math.abs(num) <= 180;

      if (
        coordinates.some(
          (coord) =>
            typeof coord !== "number" || isNaN(coord) || !isFinite(coord)
        )
      ) {
        throw new Error("Coordinates should be valid float numbers.");
      }

      const [latitude, longitude] = coordinates;

      if (!isLatitude(latitude)) {
        throw new Error("Latitude must be a number between -90 and 90.");
      }

      if (!isLongitude(longitude)) {
        throw new Error("Longitude must be a number between -180 and 180.");
      }
      return true;
    }),
  validateRequest,
  requireAuth(JWT_KEY),
  currentUser,
  ProfileController.searchProfiles
);

module.exports = router;
