const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const Project = require("../models/projects");
const Profile = require("../models/profiles");
const mongoose = require("mongoose");

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

  if (attachments.length > 0) {
    console.dir(req.body, { depth: null });
    project.attachments.push(...attachments);
  }

  if (skills.length > 0) {
    project.skills.push(...skills);
  }

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
    (project) => project._id.toString() === projectId
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

const updateProjectById = catchAsync(async (req, res, next) => {
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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  const { title, description, startDate, endDate, skills, contractId } =
    req.body;

  project.title = title || project.title;
  project.description = description || project.description;
  project.startDate = startDate || project.startDate;
  project.endDate = endDate || project.endDate;
  project.skills = skills || project.skills;
  project.contractId = contractId || project.contractId;

  await profile.save();

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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  await Project.findByIdAndDelete(project._id.toString());

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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  const { attachments } = req.body;

  project.attachments.push(...attachments);
  await profile.save();

  res.status(200).json({
    status: "success",
    data: {
      project,
    },
  });
});

const getProjectAttachments = catchAsync(async (req, res, next) => {
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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  res.status(200).json({
    status: "success",
    data: {
      attachments: project.attachments,
    },
  });
});

const getProjectAttachment = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;
  const attachmentid = req.params.attachmentid;

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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  const attachment = project.attachments.find(
    (attachment) => attachment._id.toString() === attachmentid
  );

  if (!attachment) {
    return next(
      new AppError(`No attachment found with matching id: ${attachmentid}`, 404)
    );
  }

  res.status(200).json({
    status: "success",
    data: {
      attachment,
    },
  });
});

const deleteProjectAttachment = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;
  const attachmentid = req.params.attachmentid;

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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  const attachmentIndex = project.attachments.findIndex(
    (attachment) => attachment._id.toString() === attachmentid
  );

  if (attachmentIndex === -1) {
    return next(
      new AppError(`No attachment found with matching id: ${attachmentid}`, 404)
    );
  }

  project.attachments.splice(attachmentIndex, 1);
  await profile.save();

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const deleteProjectAttachments = catchAsync(async (req, res, next) => {
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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  project.attachments = [];
  await profile.save();

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const addProjectSkill = catchAsync(async (req, res, next) => {
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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  const { skills } = req.body;

  project.skills.push(...skills);
  await profile.save();

  res.status(200).json({
    status: "success",
    data: {
      project,
    },
  });
});

const getProjectSkills = catchAsync(async (req, res, next) => {
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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  res.status(200).json({
    status: "success",
    data: {
      skills: project.skills,
    },
  });
});

const getProjectSkill = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;
  const attachmentid = req.params.attachmentid;

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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  // const attachment = project.attachments.find(
  //   (attachment) => attachment._id.toString() === attachmentid
  // );

  // if (!attachment) {
  //   return next(
  //     new AppError(`No attachment found with matching id: ${attachmentid}`, 404)
  //   );
  // }

  res.status(200).json({
    status: "success",
    data: {
      message: "Not implemented yet",
    },
  });
});

const deleteProjectSkill = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;
  const skillId = req.params.skillid;

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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  const skillIndex = project.skills.findIndex(
    (skill) => skill.id.toString() === skillId
  );

  if (skillIndex === -1) {
    return next(
      new AppError(`No skill found with matching id: ${skillId}`, 404)
    );
  }

  project.skills.splice(skillIndex, 1);
  await profile.save();

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const deleteProjectSkills = catchAsync(async (req, res, next) => {
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
    (project) => project._id.toString() === projectId
  );

  if (!project) {
    return next(
      new AppError(`No project found with matching id: ${profileId}`, 404)
    );
  }

  project.skills = [];
  await profile.save();

  res.status(204).json({
    status: "success",
    data: null,
  });
});

exports.createProject = createProject;
exports.getProjects = getProjects;
exports.getProjectById = getProjectById;
exports.deleteProjectById = deleteProjectById;
exports.updateProjectById = updateProjectById;
exports.createProjectAttachment = createProjectAttachment;
exports.getProjectAttachments = getProjectAttachments;
exports.getProjectAttachment = getProjectAttachment;
exports.deleteProjectAttachment = deleteProjectAttachment;
exports.deleteProjectAttachments = deleteProjectAttachments;
exports.addProjectSkill = addProjectSkill;
exports.getProjectSkills = getProjectSkills;
exports.getProjectSkill = getProjectSkill;
exports.deleteProjectSkill = deleteProjectSkill;
exports.deleteProjectSkills = deleteProjectSkills;
