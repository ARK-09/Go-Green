const Project = require("../models/projects");
const Profile = require("../models/profiles");

const createProject = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;

  const profile = await Profile.findById(profileId)
    .and()
    .where("active")
    .equals(true);

  if (!profile) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const isAllowed = req.currentUser.id === profile.userId;

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

  const project = await Project.create({
    title,
    description,
    startDate,
    endDate,
    attachments,
    skills,
    contractId,
  });

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
    .and()
    .where("active")
    .equals(true);

  if (!profile) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const projects = await profile.populate("projects");

  res.status(200).json({
    status: "success",
    length: projects.length,
    data: { projects },
  });
});

const getProjectById = catchAsync(async (req, res, next) => {
  const profileId = req.params.id;
  const projectId = req.params.projectid;

  const profile = await Profile.findById(profileId)
    .where("active")
    .equals(true);

  if (!profile) {
    return next(
      new AppError(`No profile found with matching id: ${profileId}`),
      404
    );
  }

  const project = (await profile.populate("projects")).projects.find(
    (id, index, project) => (id === projectId ? project : null)
  );
  console.log(project);

  res.status(200).json({
    status: "success",
    data: { project },
  });
});

const deleteProjectById = catchAsync(async (req, res, next) => {
  const profileId = req.body.id;
  const projectId = req.body.projectid;

  const profile = await Profile.findOne()
    .where("_id")
    .equals(profileId)
    .where("projects")
    .equals(projectId);

  if (!profile) {
    return next(new AppError(`No project found with id: ${projectId}`), 404);
  }

  await Project.findByIdAndDelete(project.id);
});

exports.createProject = createProject;
exports.getProjects = getProjects;
exports.getProjectById = getProjectById;
