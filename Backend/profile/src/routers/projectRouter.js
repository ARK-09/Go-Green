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
      .isArray()
      .withMessage("Attachment should be an array."),
    check("attachments.id")
      .optional()
      .custom((value, { req }) => {
        if (value && !req.body.attachments.file) {
          throw new Error(
            "If mimeType is provided, mimeType should also be given."
          );
        }
        return true;
      })
      .isMongoId()
      .withMessage("Attachments id should be valid id."),
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
