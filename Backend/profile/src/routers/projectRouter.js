const express = require("express");
const ProjectController = require("../controllers/projectsController");
const currentUser = require("../middelwares/currentUser");
const { requireAuth } = require("@ark-industries/gogreen-common");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

router
  .route("/")
  .post(requireAuth(JWT_KEY), currentUser, ProjectController.createProject)
  .get(requireAuth(JWT_KEY), currentUser, ProjectController.getProjects);

router
  .route("/:projectid")
  .get(requireAuth(JWT_KEY), currentUser, ProjectController.getProjectById)
  .patch(requireAuth(JWT_KEY), currentUser, ProjectController.updateProjectById)
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectById
  );

router
  .route("/:projectid/attachments")
  .get(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjectAttachments
  )
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectAttachments
  );

router
  .route("/:projectid/attachments/attachmentid")
  .get(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.getProjectAttachment
  )
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectAttachment
  );

router
  .route("/:projectid/skills")
  .get(requireAuth(JWT_KEY), currentUser, ProjectController.getProjectSkills)
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectSkills
  );

router
  .route("/:projectid/skills/skillid")
  .get(requireAuth(JWT_KEY), currentUser, ProjectController.getProjectSkill)
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectSkill
  );

module.exports = router;
