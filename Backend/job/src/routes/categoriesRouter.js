const express = require("express");
const { body, param } = require("express-validator");
const {
  requireAuth,
  validateRequest,
  restrictTo,
} = require("@ark-industries/gogreen-common");
const currentUser = require("../middelwares/currentUser");
const CategoryController = require("../controllers/categoriesController");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

// Validation middleware for createCategories route
const validateCreateCategories = [
  body("categories")
    .isArray({ min: 1 })
    .withMessage("Categories must be an array with at least one element.")
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
  body("categories.*")
    .isString()
    .withMessage("Category should be a valid string."),
];

// Validation middleware for updateCategory route
const validateUpdateCategory = [
  param("id").isMongoId().withMessage("Please provide a valid id."),
  body("title").notEmpty().withMessage("Title field is required.").escape(),
];

const validateGetCategory = [
  param("id").isMongoId().withMessage("Please provide a valid id."),
];

const validateDeleteCategory = [
  param("id").isMongoId().withMessage("Please provide a valid id."),
];

// Routes
router
  .route("/")
  .post(
    validateCreateCategories,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    CategoryController.createCategories
  )
  .get(requireAuth(JWT_KEY), currentUser, CategoryController.getCategories)
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    CategoryController.deleteCategories
  );

router
  .route("/:id")
  .get(
    validateGetCategory,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    CategoryController.getCategory
  )
  .patch(
    validateUpdateCategory,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    CategoryController.updateCategory
  )
  .delete(
    validateDeleteCategory,
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    restrictTo("admin"),
    CategoryController.deleteCategory
  );

module.exports = router;
