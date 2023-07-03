const Skill = require("../models/skills");
const {
  catchAsync,
  AppError,
  natsWrapper,
} = require("@ark-industries/gogreen-common");

const SkillCreatedPublisher = require("../events/skillCreatedPublisher");
const SkillUpdatedPublisher = require("../events/skillUpdatedPublisher");
const SkillDeletedPublisher = require("../events/skillDeletedPublisher");

const createSkills = catchAsync(async (req, res, next) => {
  const { skills } = req.body;
  const skillsObjectsArray = skills.map((title) => {
    return {
      title,
    };
  });

  const skillsDb = await Skill.create(skillsObjectsArray);

  await new SkillCreatedPublisher(natsWrapper.client)
    .publish(skillsDb)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: {
      skills: skillsDb,
    },
  });
});

const getSkills = catchAsync(async (req, res, next) => {
  const skills = await Skill.find({});

  res.status(200).json({
    status: "success",
    data: {
      skills,
    },
  });
});

const getSkill = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const skill = await Skill.findById(id);

  if (!skill) {
    return next(new AppError(`No skill found with matching id: ${id}`, 404));
  }

  res.status(200).json({
    status: "success",
    data: {
      skill,
    },
  });
});

const updateSkill = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const { title } = req.body;

  const skill = await Skill.findByIdAndUpdate(id, { title }, { new: true });

  if (!skill) {
    return next(new AppError(`No skill found with id: ${id}`, 404));
  }

  await new SkillUpdatedPublisher(natsWrapper.client)
    .publish(skill)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: skill,
  });
});

const deleteSkill = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const skill = await Skill.findById(id);

  if (!skill) {
    return next(new AppError(`No skill found with matching id: ${id}`, 404));
  }

  await Skill.findByIdAndDelete(id);
  await new SkillDeletedPublisher(natsWrapper.client).publish(id).catch(() => {
    return next(new AppError("Something went wrong...", 500));
  });

  res.status(200).json({
    status: "success",
    data: null,
  });
});

const deleteSkills = catchAsync(async (req, res, next) => {
  await Skill.deleteMany({});

  await new SkillDeletedPublisher(natsWrapper.client)
    .publish({ all: true })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: null,
  });
});

exports.createSkills = createSkills;
exports.getSkills = getSkills;
exports.getSkill = getSkill;
exports.updateSkill = updateSkill;
exports.deleteSkill = deleteSkill;
exports.deleteSkills = deleteSkills;
