const express = require("express");
const { body, param } = require("express-validator");
const {
  requireAuth,
  validateRequest,
  restrictTo,
} = require("@ark-industries/gogreen-common");
const currentUser = require("../middelwares/currentUser");
const SkillController = require("../controllers/skillsController");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

// Validation middleware for createSkills route
const validateCreateSkills = [
  body("skills")
    .isArray({ min: 1 })
    .withMessage("Skills must be an array with at least one element.")
    .customSanitizer((value) => {
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
  body("skills.*").isString().withMessage("Skill should be a valid string."),
];

// Validation middleware for updateSkill route
const validateUpdateSkill = [
  param("id").isMongoId().withMessage("Please provide a valid id."),
  body("title").notEmpty().withMessage("Title field is required.").escape(),
];

const validateGetSkill = [
  param("id").isMongoId().withMessage("Please provide a valid id."),
];

const validateDeleteSkill = [
  param("id").isMongoId().withMessage("Please provide a valid id."),
];

// Routes
router
  .route("/")
  .post(
    validateCreateSkills,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    SkillController.createSkills
  )
  .get(requireAuth(JWT_KEY), currentUser, SkillController.getSkills)
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    SkillController.deleteSkills
  );

router
  .route("/:id")
  .get(
    validateGetSkill,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    SkillController.getSkill
  )
  .patch(
    validateUpdateSkill,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    SkillController.updateSkill
  )
  .delete(
    validateDeleteSkill,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    SkillController.deleteSkill
  );

module.exports = router;
