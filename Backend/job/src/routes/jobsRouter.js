const express = require("express");
const { check, param, query, sanitizeBody } = require("express-validator");
const {
  requireAuth,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const currentUser = require("../middelwares/currentUser");
const JobsController = require("../controllers/jobsController");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

router
  .route("/")
  .post(
    check("title").notEmpty().withMessage("Title field is required."),
    check("description")
      .notEmpty()
      .withMessage("Description field is required."),
    check("categories")
      .isArray({ min: 1 })
      .withMessage(
        "Categories should be provided as an array with at least one category id."
      ),
    check("categories.*")
      .isMongoId()
      .withMessage("Invalid category ID. Please provide a valid MongoDB ID."),
    check("budget")
      .isFloat({ min: 5 })
      .withMessage("Budget should be a number."),
    check("expectedDuration")
      .isString()
      .withMessage("Expected duration should be of type string.")
      .isIn(
        "Less than 1 month",
        "1 to 3 months",
        "3 to 6 months",
        "More than 6 months"
      )
      .withMessage(
        "Please provide a valid expected duration. Valid options: 'Less than 1 month', '1 to 3 months', '3 to 6 months', 'More than 6 months'."
      ),
    check("paymentType")
      .notEmpty()
      .withMessage("Payment type field is required.")
      .isIn("hourly", "fixed")
      .withMessage("Payment type must be one of: 'hourly', 'fixed'."),
    check("attachments.mimeType")
      .optional()
      .custom((value, { req }) => {
        if (value && !req.body.attachments.file) {
          throw new Error(
            "If mimeType is provided, file url should also be given."
          );
        }
        return true;
      })
      .isString()
      .withMessage("Attachments mimeType should be of type string.")
      .isIn(
        "image/jpeg",
        "image/png",
        "image/gif",
        "video/mp4",
        "video/mpeg",
        "video/quicktime"
      )
      .withMessage(
        "Invalid MIME type provided. Only the following MIME types are allowed: image/jpeg, image/png, image/gif, video/mp4, video/quicktime."
      ),
    check("attachments.file")
      .optional()
      .custom((value, { req }) => {
        if (value && !req.body.attachments.mimeType) {
          throw new Error(
            "If file is provided, mimeType should also be given."
          );
        }
        return true;
      })
      .isURL()
      .withMessage("Attachments file should be a valid URL."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.createJob
  )
  .get(requireAuth(JWT_KEY), currentUser, JobsController.getJobs);

router.route("/search").get(
  query("query")
    .optional()
    .isString()
    .withMessage("Query should be of type string."),
  query("offset")
    .optional()
    .isInt({ min: 1 })
    .withMessage("Offset should be an integer with min value 1."),
  query("limit")
    .optional()
    .isInt({ min: 5 })
    .withMessage("Limit should be an integer with min value 5."),
  query("location.latitude")
    .optional()
    .custom((value, { req }) => {
      if (value && !req.body.location.longitude) {
        throw new Error(
          "If latitude is provided, longitude should also be given."
        );
      }
      return true;
    })
    .isFloat()
    .withMessage("Latitude should be a float number."),
  query("location.longitude")
    .optional()
    .custom((value, { req }) => {
      if (value && !req.body.location.latitude) {
        throw new Error(
          "If longitude is provided, latitude should also be given."
        );
      }
      return true;
    })
    .isFloat()
    .withMessage("Longitude should be a float number."),
  sanitizeBody("*").escape(),
  validateRequest,
  JobsController.searchJobs
);

router
  .route("/:id")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.getJob
  )
  .patch(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    check("title").notEmpty().withMessage("Title field is required."),
    check("description")
      .notEmpty()
      .withMessage("Description field is required."),
    check("categories")
      .isArray({ min: 1 })
      .withMessage(
        "Categories should be provided as an array with at least one category id."
      ),
    check("categories.*")
      .isMongoId()
      .withMessage("Invalid category ID. Please provide a valid MongoDB ID."),
    check("budget")
      .isFloat({ min: 5 })
      .withMessage("Budget should be a number."),
    check("expectedDuration")
      .isString()
      .withMessage("Expected duration should be of type string.")
      .isIn(
        "Less than 1 month",
        "1 to 3 months",
        "3 to 6 months",
        "More than 6 months"
      )
      .withMessage(
        "Please provide a valid expected duration. Valid options: 'Less than 1 month', '1 to 3 months', '3 to 6 months', 'More than 6 months'."
      ),
    check("paymentType")
      .notEmpty()
      .withMessage("Payment type field is required.")
      .isIn("hourly", "fixed")
      .withMessage("Payment type must be one of: 'hourly', 'fixed'."),
    check("attachments.mimeType")
      .optional()
      .custom((value, { req }) => {
        if (value && !req.body.attachments.file) {
          throw new Error(
            "If mimeType is provided, file url should also be given."
          );
        }
        return true;
      })
      .isString()
      .withMessage("Attachments mimeType should be of type string.")
      .isIn(
        "image/jpeg",
        "image/png",
        "image/gif",
        "video/mp4",
        "video/mpeg",
        "video/quicktime"
      )
      .withMessage(
        "Invalid MIME type provided. Only the following MIME types are allowed: image/jpeg, image/png, image/gif, video/mp4, video/quicktime."
      ),
    check("attachments.file")
      .optional()
      .custom((value, { req }) => {
        if (value && !req.body.attachments.mimeType) {
          throw new Error(
            "If file is provided, mimeType should also be given."
          );
        }
        return true;
      })
      .isURL()
      .withMessage("Attachments file should be a valid URL."),
    check("status")
      .notEmpty()
      .withMessage("Status field is required.")
      .isIn("Open", "Assigned", "Completed", "Canceled", "Disputed")
      .withMessage(
        "Status must be one of: Draft, Open, In Review, Assigned, Completed, Canceled, Disputed."
      ),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.updateJob
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.deleteJob
  );

router
  .route("/:id/attachments")
  .post(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    check("attachments.mimeType")
      .optional()
      .custom((value, { req }) => {
        if (value && !req.body.attachments.file) {
          throw new Error(
            "If mimeType is provided, file url should also be given."
          );
        }
        return true;
      })
      .isString()
      .withMessage("Attachments mimeType should be of type string.")
      .isIn(
        "image/jpeg",
        "image/png",
        "image/gif",
        "video/mp4",
        "video/mpeg",
        "video/quicktime"
      )
      .withMessage(
        "Invalid MIME type provided. Only the following MIME types are allowed: image/jpeg, image/png, image/gif, video/mp4, video/quicktime."
      ),
    check("attachments.file")
      .optional()
      .custom((value, { req }) => {
        if (value && !req.body.attachments.mimeType) {
          throw new Error(
            "If file is provided, mimeType should also be given."
          );
        }
        return true;
      })
      .isURL()
      .withMessage("Attachments file should be a valid URL."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.createJobAttachment
  )
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.getJobAttachments
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.deleteJobAttachments
  );

router
  .route("/:id/attachments/:attachmentid")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    param("attachmentid")
      .isMongoId()
      .withMessage("Invalid attachment ID. Please provide a valid MongoDB ID."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.getJob
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    param("attachmentid")
      .isMongoId()
      .withMessage("Invalid attachment ID. Please provide a valid MongoDB ID."),
    sanitizeBody("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.deleteJob
  );

module.exports = router;
