const express = require("express");
const { check, param, query } = require("express-validator");
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
    check("title").escape().notEmpty().withMessage("Title field is required."),
    check("description")
      .escape()
      .notEmpty()
      .withMessage("Description field is required."),
    check("talentCount")
      .optional()
      .isInt({ min: 1 })
      .withMessage("The required number of talents must be greater than zero."),
    check("categories")
      .isArray({ min: 1 })
      .withMessage(
        "Categories should be provided as an array with at least one category id."
      ),
    check("categories.*")
      .isMongoId()
      .withMessage("Invalid category ID. Please provide a valid MongoDB ID."),
    check("skills")
      .isArray({ min: 1 })
      .withMessage(
        "Skills should be provided as an array with at least one skill id."
      ),
    check("skills.*")
      .isMongoId()
      .withMessage("Invalid skill ID. Please provide a valid MongoDB ID."),
    check("budget")
      .isFloat({ min: 5 })
      .withMessage("Budget should be a number."),
    check("expectedDuration")
      .notEmpty()
      .withMessage("Expected duration is required.")
      .isIn([
        "Less than 1 month",
        "1 to 3 months",
        "3 to 6 months",
        "More than 6 months",
      ])
      .withMessage(
        "Please provide a valid expected duration. Valid options: 'Less than 1 month', '1 to 3 months', '3 to 6 months', 'More than 6 months'."
      ),
    check("paymentType")
      .notEmpty()
      .withMessage("Payment type field is required.")
      .isIn(["hourly", "fixed"])
      .withMessage("Payment type must be one of: 'hourly', 'fixed'."),
    check("attachments")
      .optional()
      .isArray({ min: 1 })
      .withMessage("Attachments should be an array with min 1 attachment."),
    check("attachments.*.id")
      .notEmpty()
      .withMessage("Attachment ID is required")
      .isMongoId()
      .withMessage("Attachment ID should be a valid MongoDB ID"),

    check("attachments.*.mimeType")
      .notEmpty()
      .withMessage("Attachment MIME type is required")
      .isIn([
        "image/jpeg",
        "image/png",
        "image/gif",
        "video/mp4",
        "video/mpeg",
        "video/quicktime",
      ])
      .withMessage(
        "Invalid Attachment MIME type provided. Only the following MIME types are allowed: image/jpeg, image/png, image/gif, video/mp4, video/quicktime."
      ),
    check("attachments.*.originalName")
      .notEmpty()
      .withMessage("Attachment Original name is required"),
    check("attachments.*.createdDate")
      .notEmpty()
      .withMessage("Attachment Created date is required")
      .isISO8601()
      .withMessage("Attachment Created date should be in ISO 8601 format"),
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
          location.coordinates.some(
            (coord) =>
              typeof coord !== "number" || isNaN(coord) || !isFinite(coord)
          )
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
    .withMessage("Query should be of type string.")
    .escape(),
  query("offset")
    .optional()
    .isInt({ min: 1 })
    .withMessage("Offset should be an integer with min value 1."),
  query("limit")
    .optional()
    .isInt({ min: 5 })
    .withMessage("Limit should be an integer with min value 5."),
  query("price")
    .optional()
    .isFloat({ min: 5 })
    .withMessage("Price should be an integer with min value 5."),
  query("location")
    .optional()
    .isObject()
    .withMessage("Location should be an object.")
    .bail()
    .if(query("location.coordinates").exists())
    .isArray({ min: 2, max: 2 })
    .withMessage(
      "Coordinates should be an array containing exactly two elements: [latitude, longitude]."
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
  JobsController.searchJobs
);

router
  .route("/:id")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.getJob
  )
  .patch(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    check("title")
      .optional()
      .escape()
      .notEmpty()
      .withMessage("Title field is required."),
    check("description")
      .optional()
      .notEmpty()
      .withMessage("Description field is required.")
      .escape(),
    check("categories")
      .optional()
      .isArray({ min: 1 })
      .withMessage(
        "Categories should be provided as an array with at least one category id."
      ),
    check("categories.*")
      .isMongoId()
      .withMessage("Invalid category ID. Please provide a valid MongoDB ID."),
    check("skills")
      .optional()
      .isArray({ min: 1 })
      .withMessage(
        "Skills should be provided as an array with at least one skill id."
      ),
    check("skills.*")
      .isMongoId()
      .withMessage("Invalid skill ID. Please provide a valid MongoDB ID."),
    check("budget")
      .optional()
      .isFloat({ min: 5 })
      .withMessage("Budget should be a number."),
    check("expectedDuration")
      .optional()
      .isString()
      .withMessage("Expected duration should be of type string.")
      .isIn([
        "Less than 1 month",
        "1 to 3 months",
        "3 to 6 months",
        "More than 6 months",
      ])
      .withMessage(
        "Please provide a valid expected duration. Valid options: 'Less than 1 month', '1 to 3 months', '3 to 6 months', 'More than 6 months'."
      ),
    check("paymentType")
      .optional()
      .notEmpty()
      .withMessage("Payment type field is required.")
      .isIn(["hourly", "fixed"])
      .withMessage("Payment type must be one of: 'hourly', 'fixed'."),
    check("attachments")
      .optional()
      .isArray({ min: 1 })
      .withMessage("Attachments should be an array with min 1 attachment."),
    check("attachments.*.id")
      .notEmpty()
      .withMessage("Attachment ID is required")
      .isMongoId()
      .withMessage("Attachment ID should be a valid MongoDB ID"),

    check("attachments.*.mimeType")
      .notEmpty()
      .withMessage("Attachment MIME type is required")
      .isIn([
        "image/jpeg",
        "image/png",
        "image/gif",
        "video/mp4",
        "video/mpeg",
        "video/quicktime",
      ])
      .withMessage(
        "Invalid Attachment MIME type provided. Only the following MIME types are allowed: image/jpeg, image/png, image/gif, video/mp4, video/quicktime."
      ),

    check("attachments.*.originalName")
      .notEmpty()
      .withMessage("Attachment Original name is required"),

    check("attachments.*.createdDate")
      .notEmpty()
      .withMessage("Attachment Created date is required")
      .isISO8601()
      .withMessage("Attachment Created date should be in ISO 8601 format"),
    check("status")
      .optional()
      .notEmpty()
      .withMessage("Status field is required.")
      .isIn(["Open", "Assigned", "Completed", "Canceled", "Disputed"])
      .withMessage(
        "Status must be one of: Draft, Open, In Review, Assigned, Completed, Canceled, Disputed."
      ),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.updateJob
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
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
    check("attachments")
      .isArray({ min: 1 })
      .withMessage("Attachments should be an array with min 1 attachment."),
    check("attachments.*.id")
      .notEmpty()
      .withMessage("ID is required")
      .isMongoId()
      .withMessage("ID should be a valid MongoDB ID"),

    check("attachments.*.mimeType")
      .notEmpty()
      .withMessage("MIME type is required")
      .isIn([
        "image/jpeg",
        "image/png",
        "image/gif",
        "video/mp4",
        "video/mpeg",
        "video/quicktime",
      ])
      .withMessage(
        "Invalid MIME type provided. Only the following MIME types are allowed: image/jpeg, image/png, image/gif, video/mp4, video/quicktime."
      ),

    check("attachments.*.originalName")
      .notEmpty()
      .withMessage("Original name is required"),

    check("attachments.*.createdDate")
      .notEmpty()
      .withMessage("Created date is required")
      .isISO8601()
      .withMessage("Created date should be in ISO 8601 format"),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.createJobAttachment
  )
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.getJobAttachments
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
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
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.getJobAttachment
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    param("attachmentid")
      .isMongoId()
      .withMessage("Invalid attachment ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    JobsController.deleteJobAttachment
  );

module.exports = router;
