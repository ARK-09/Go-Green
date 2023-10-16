const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const extractValidProperties = require("../util/extractValidProperties");
const Project = require("../models/projects");
const Profile = require("../models/profiles");

const createProject = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;

  const profile = await Profile.findById(profileId);

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
    req.currentUser.userType === "talent";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const { title, description, startDate, endDate, skills, contractId } =
    req.body;

  let { attachments } = req.body;

  attachments = extractValidProperties(attachments, [
    "id",
    "mimeType",
    "originalName",
    "createdDate",
  ]);

  const project = new Project({
    title,
    description,
    startDate,
    endDate,
    contractId,
  });

  if (attachments && Array.isArray(attachments)) {
    project.attachments.push(...attachments);
  } else if (typeof attachments === "object") {
    project.attachments.push(attachments);
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

  const profile = await Profile.findById(profileId)
    .sort({ createdDate: 1 })
    .populate("projects");

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  res.status(200).json({
    status: "success",
    length: profile.projects.length,
    data: { projects: profile.projects },
  });
});

const getProjectById = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;

  const profile = await Profile.findById(profileId);

  if (!profile || !profile.active) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`, 404)
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
  project.skills = skills.length > 0 || project.skills;
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    (req.currentUser._id.toString() === profile.userId.toString() &&
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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

  let { attachments } = req.body;

  attachments = extractValidProperties(attachments, [
    "id",
    "mimeType",
    "originalName",
    "createdDate",
  ]);

  if (attachments && Array.isArray(attachments) && attachments.length > 0) {
    project.attachments.push(...attachments);
  } else if (typeof attachments === "object") {
    project.attachments.push(attachments);
  }

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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
    (attachment) => attachment.id.toString() === attachmentid
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
    (attachment) => attachment.id.toString() === attachmentid
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
  //   (attachment) => attachment.id.toString() === attachmentid
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
    (skill) => skill._id.toString() === skillId
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
      new AppError(`No profile found with matching id: ${profileId}`, 404)
    );
  }

  const isAllowed =
    req.currentUser._id.toString() === profile.userId.toString() &&
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
