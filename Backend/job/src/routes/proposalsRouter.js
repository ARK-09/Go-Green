const express = require("express");
const { check, param } = require("express-validator");
const {
  requireAuth,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const currentUser = require("../middelwares/currentUser");
const ProposalController = require("../controllers/proposalController");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

router
  .route("/")
  .get(requireAuth(JWT_KEY), currentUser, ProposalController.getProposals);

router
  .route("/users/:userid/reviews")
  .get(
    param("userid")
      .isMongoId()
      .withMessage("Invalid user ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.getReviews
  );

router
  .route("/jobs/:id")
  .post(
    param("id")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    check("bidAmount")
      .isFloat({ min: 5 })
      .withMessage("Bid amount should be a number having min value 5."),
    check("coverLetter")
      .notEmpty()
      .withMessage("Cover letter field is required.")
      .escape(),
    check("proposedDuration")
      .notEmpty()
      .withMessage("Proposed duration field is required.")
      .isIn([
        "Less than 1 month",
        "1 to 3 months",
        "3 to 6 months",
        "More than 6 months",
      ])
      .withMessage(
        "Please provide a valid proposed duration. Valid options: 'Less than 1 month', '1 to 3 months', '3 to 6 months', 'More than 6 months'."
      ),
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
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.createJobProposal
  )
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.getJobProposals
  );

router
  .route("/services/:id")
  .post(
    param("id")
      .isMongoId()
      .withMessage("Invalid service ID. Please provide a valid MongoDB ID."),
    check("bidAmount").notEmpty().withMessage("Bid amount field is required."),
    check("coverLetter")
      .notEmpty()
      .withMessage("Cover letter field is required.")
      .escape(),
    check("proposedDuration")
      .notEmpty()
      .withMessage("Proposed duration field is required."),
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
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.createServiceProposal
  )
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid service ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.getServiceProposals
  );

router
  .route("/:id")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.getProposal
  )
  .post(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    check("rating")
      .isFloat({ min: 0.0, max: 5.0 })
      .withMessage("Rating should be between 0.0 to 5.0"),
    check("feedback").notEmpty().withMessage("Feedback is required.").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.createJobProposalFeedback
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.deleteProposal
  );

router
  .route("/:jobid")
  .get(
    param("jobid")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.isAlreadyApplied
  );

router
  .route("/:id/jobs/:jobid")
  .patch(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    param("jobid")
      .isMongoId()
      .withMessage("Invalid job ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.hireProposal
  );

router
  .route("/:id/interviews")
  .post(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.createInterview
  );

router
  .route("/:id/attachments")
  .post(
    check("attachments")
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
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.createProposalAttachment
  )
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.getProposalAttachments
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.deleteProposalAttachments
  );

router
  .route("/:id/attachments/:attachmentid")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    param("attachmentid")
      .isMongoId()
      .withMessage("Invalid attachment ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.getProposalAttachment
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    param("attachmentid")
      .isMongoId()
      .withMessage("Invalid attachment ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProposalController.deleteProposalAttachment
  );

module.exports = router;
