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
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    ProjectController.deleteProjectById
  );

module.exports = router;
