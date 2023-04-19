const express = require("express");
const ProjectController = require("../controllers/projectsController");

const router = express.Router();

router
  .route("/")
  .post(ProjectController.createProject)
  .get(ProjectController.getProjects);

router.route("/:projectid").get(ProjectController.getProjectById);

module.exports = router;
