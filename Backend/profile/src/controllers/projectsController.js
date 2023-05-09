const mongoose = require("mongoose");
const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const Project = require("../models/projects");
const Profile = require("../models/profiles");

const createProject = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;

  const profile = await Profile.findById(profileId);

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 204)
    );
  }

  const isAllowed =
    req.currentUser.id === profile.userId.toString() &&
    req.currentUser.userType === "talent";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const {
    title,
    description,
    startDate,
    endDate,
    attachments,
    skills,
    contractId,
  } = req.body;

  const project = new Project({
    title,
    description,
    startDate,
    endDate,
    contractId,
  });

  project.attachments.push(...attachments);
  project.skills.push(...skills);
  await project.save();

  profile.projects.push(project._id);
  await profile.save();

  res.status(201).json({
    status: "success",
    data: { project },
  });
});

const getProjects = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;

  const profile = await Profile.findById(profileId);

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 204)
    );
  }

  const projects = (await profile.populate("projects")).projects;

  res.status(200).json({
    status: "success",
    length: projects.length,
    data: { projects },
  });
});

const getProjectById = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;

  const profile = await Profile.findById(profileId);

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 204)
    );
  }

  const project = (await profile.populate("projects")).projects.find(
    (project) => project.id === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  res.status(200).json({
    status: "success",
    data: { project },
  });
});

const deleteProjectById = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;

  const profile = await Profile.findById(profileId);

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 204)
    );
  }

  const isAllowed =
    (req.currentUser.id === profile.userId.toString() &&
      req.currentUser.userType === "talent") ||
    req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const project = (await profile.populate("projects")).projects.find(
    (project) => project.id === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  await Project.findByIdAndDelete(project.id.toString());

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const createProjectAttachment = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;

  const profile = await Profile.findById(profileId);

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 204)
    );
  }

  const isAllowed =
    req.currentUser.id === profile.userId.toString() &&
    req.currentUser.userType === "talent";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const project = (await profile.populate("projects")).projects.find(
    (project) => project.id === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }
});

exports.createProject = createProject;
exports.getProjects = getProjects;
exports.getProjectById = getProjectById;
exports.deleteProjectById = deleteProjectById;
exports.createProjectAttachment = createProjectAttachment;
