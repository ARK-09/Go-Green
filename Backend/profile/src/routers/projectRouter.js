const express = require("express");
const ProjectController = require("../controllers/projectsController");
const currentUser = require("../middelwares/currentUser");
const {
  requireAuth,
  validateRequest,
} = require("@ark-industries/gogreen-common");

const { check, param } = require("express-validator");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

router
  .route("/")
  .post(
    check("title").notEmpty().withMessage("Title field is required."),
    check("description")
      .notEmpty()
      .withMessage("Description field is required."),
    check("startDate")
      .notEmpty()
      .withMessage("Start date field is required.")
      .isISO8601()
      .toDate()
      .withMessage(
        "Invalid start date. Please provide a valid date in ISO 8601 format."
      ),
    check("endDate")
      .notEmpty()
      .withMessage("End date field is required.")
      .isISO8601()
      .toDate()
      .withMessage(
        "Invalid end date. Please provide a valid date in ISO 8601 format."
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

    check("attachments.*.url")
      .isURL({
        protocols: ["https"],
        host_whitelist: ["gogreen-files-bucket.s3.ap-south-1.amazonaws.com"],
      })
      .withMessage("Please provide a valid image url.")
      .notEmpty()
      .withMessage("Attachment URL is required"),

    check("attachments.*.createdDate")
      .notEmpty()
      .withMessage("Attachment Created date is required")
      .isISO8601()
      .withMessage("Attachment Created date should be in ISO 8601 format"),
    check("skills").isArray().withMessage("Skills field should be an array."),
    check("skills.*")
      .isMongoId()
      .withMessage("Skills field should be an array with valid id's"),
    check("contractId")
      .isMongoId()
      .withMessage("Contract ID should be a valid id."),
    check("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.createProject
  )
  .get(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjects
  );

router
  .route("/:projectid")
  .get(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjectById
  )
  .patch(
    check("title").notEmpty().withMessage("Title field is required."),
    check("description")
      .notEmpty()
      .withMessage("Description field is required."),
    check("startDate")
      .notEmpty()
      .withMessage("Start date field is required.")
      .isISO8601()
      .toDate()
      .withMessage(
        "Invalid start date. Please provide a valid date in ISO 8601 format."
      ),
    check("endDate")
      .notEmpty()
      .withMessage("End date field is required.")
      .isISO8601()
      .toDate()
      .withMessage(
        "Invalid end date. Please provide a valid date in ISO 8601 format."
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

    check("attachments.*.url")
      .isURL({
        protocols: ["https"],
        host_whitelist: ["gogreen-files-bucket.s3.ap-south-1.amazonaws.com"],
      })
      .withMessage("Please provide a valid image url.")
      .notEmpty()
      .withMessage("Attachment URL is required"),

    check("attachments.*.createdDate")
      .notEmpty()
      .withMessage("Attachment Created date is required")
      .isISO8601()
      .withMessage("Attachment Created date should be in ISO 8601 format"),
    check("skills").isArray().withMessage("Skills field should be an array."),
    check("skills.*")
      .isMongoId()
      .withMessage("Skills field should be an array with valid id's"),
    check("contractId")
      .isMongoId()
      .withMessage("Contract ID should be a valid id."),
    check("*").escape(),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.updateProjectById
  )
  .delete(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectById
  );

router
  .route("/:projectid/attachments")
  .get(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjectAttachments
  )
  .delete(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectAttachments
  );

router
  .route("/:projectid/attachments/attachmentid")
  .get(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    param("attachmentid")
      .isMongoId()
      .withMessage("Attachment id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjectAttachment
  )
  .delete(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    param("attachmentid")
      .isMongoId()
      .withMessage("Attachment id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectAttachment
  );

router
  .route("/:projectid/skills")
  .get(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjectSkills
  )
  .delete(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectSkills
  );

router
  .route("/:projectid/skills/skillid")
  .get(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    param("skillid").isMongoId().withMessage("Skill id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjectSkill
  )
  .delete(
    param("id").isMongoId().withMessage("Profile id should be a valid id."),
    param("projectid")
      .isMongoId()
      .withMessage("Project id should be a valid id."),
    param("skillid").isMongoId().withMessage("Skill id should be a valid id."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectSkill
  );

module.exports = router;
