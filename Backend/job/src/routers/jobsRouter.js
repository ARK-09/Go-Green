const express = require("express");
const currentUser = require("../middelwares/currentUser");
const { requireAuth, restrictTo } = require("@ark-industries/gogreen-common");
const JobsController = require("../controllers/jobsController");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router();

router.route("/").post(JobsController.createJob).get(JobsController.getJobs);

router.route("/search").get(JobsController.searchJobs);

router
  .route("/:id")
  .get(JobsController.getJob)
  .patch(JobsController.updateJob)
  .delete(JobsController.deleteJob);

router
  .route("/:id/attachments")
  .post(JobsController.createJobAttachment)
  .get(JobsController.getJobAttachments)
  .delete(JobsController.deleteJobAttachments);

router
  .route("/:id/attachments/:attachmentid")
  .get(JobsController.getJob)
  .delete(JobsController.deleteJob);

module.exports = router;
