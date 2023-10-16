const express = require("express");
const mongoose = require("mongoose");
const { body, param, query } = require("express-validator");
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
const validateCategoriesArray = (value) => {
  if (!Array.isArray(value)) {
    throw new Error("Invalid format: Expected an array of categories");
  }

  for (const arrayItem of value) {
    if (!Array.isArray(arrayItem)) {
      throw new Error(
        "Invalid format: Nested item should be an array of categories"
      );
    }

    for (const nestedItem of arrayItem) {
      if (!mongoose.Types.ObjectId.isValid(nestedItem)) {
        throw new Error("Invalid category: Not a valid MongoDB ObjectId");
      }
    }
  }

  return true;
};

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
  body("categories").custom(validateCategoriesArray),
];

// Validation middleware for updateSkill route
const validateUpdateSkill = [
  param("id").isMongoId().withMessage("Please provide a valid id."),
  body("title").notEmpty().withMessage("Title field is required.").escape(),
  body("categories")
    .isArray({ min: 1 })
    .withMessage("Categories should be array of mongoIds")
    .optional(),
  body("categories.*")
    .isMongoId()
    .withMessage("Category should be a valid mongoId"),
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
  .route("/categories")
  .get(
    query("ids").isArray({ min: 1 }).withMessage("ids should be array"),
    query("ids.*").isMongoId().withMessage("Titles should be array of strings"),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    SkillController.getSkillsByCategories
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
