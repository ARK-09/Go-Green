const Skill = require("../models/skills");
const Category = require("../models/categories");
const {
  catchAsync,
  AppError,
  natsWrapper,
} = require("@ark-industries/gogreen-common");

const SkillCreatedPublisher = require("../events/skillCreatedPublisher");
const SkillUpdatedPublisher = require("../events/skillUpdatedPublisher");
const SkillDeletedPublisher = require("../events/skillDeletedPublisher");

const createSkills = catchAsync(async (req, res, next) => {
  const { skills, categories } = req.body;

  let skillsObjectsArray = skills.map((title) => {
    return {
      title,
    };
  });

  if (categories) {
    const validCategories = await validateArrayOfCategories(categories);
    skillsObjectsArray = skillsObjectsArray.map((skill, index) => {
      return (skill = {
        title: skill.title,
        categories: validCategories[index],
      });
    });
  }

  const skillsDb = await Skill.create(skillsObjectsArray);

  await new SkillCreatedPublisher(natsWrapper.client)
    .publish(skillsDb)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const populatedSkills = await Promise.all(
    skillsDb.map(async (skill) => {
      return await skill.populate("categories");
    })
  );

  res.status(200).json({
    status: "success",
    data: {
      skills: populatedSkills,
    },
  });
});

const getSkills = catchAsync(async (req, res, next) => {
  const skills = await Skill.find({}).populate("categories");

  res.status(200).json({
    status: "success",
    data: {
      skills,
    },
  });
});

const getSkillsByCategories = catchAsync(async (req, res, next) => {
  const { ids } = req.query;

  const query = {
    _id: { $in: ids },
  };

  const skills = await Skill.find({}).populate({
    path: "categories",
    match: query,
  });

  const filteredSkills = skills.filter((skill) => {
    return skill.categories.length > 0;
  });

  res.status(200).json({
    status: "success",
    data: {
      skills: filteredSkills,
    },
  });
});

const getSkill = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const skill = await Skill.findById(id);

  if (!skill) {
    return next(new AppError(`No skill found with matching id: ${id}`, 404));
  }

  const populatedSkill = await skill.populate("categories");

  res.status(200).json({
    status: "success",
    data: {
      skill: populatedSkill,
    },
  });
});

const updateSkill = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const { title, categories } = req.body;

  let validCategories = [];
  if (categories) {
    validCategories = await validateCategories(categories);
  }

  const skill = await Skill.findById(id);

  if (!skill) {
    return next(new AppError(`No skill found with id: ${id}`, 404));
  }

  skill.title = title;
  const uniqueCategories = [
    ...new Set([
      ...skill.categories.map((category) => category.toString()),
      ...validCategories.map((category) => category.toString()),
    ]),
  ];

  console.log(uniqueCategories);

  skill.categories = uniqueCategories;
  await skill.save();

  await new SkillUpdatedPublisher(natsWrapper.client)
    .publish(skill)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  const populatedSkill = await skill.populate("categories");

  res.status(200).json({
    status: "success",
    data: { skill: populatedSkill },
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

  res.status(204).json({
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

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const validateArrayOfCategories = async (categories) => {
  let categoriesResult = [];

  for (const currentCategoriesArray of categories) {
    const validCategories = await Category.find({
      _id: { $in: currentCategoriesArray },
    });

    const invalidCategories = validCategories.filter(
      (category) => !currentCategoriesArray.includes(category._id.toString())
    );

    if (validCategories.length < 1 || invalidCategories.length > 0) {
      throw new AppError(
        `Following categories are not found in nested array: ${
          validCategories.length < 1
            ? currentCategoriesArray.join(", ")
            : invalidCategories
                .map((category) => category._id.toString())
                .join(", ")
        }`,
        404
      );
    }

    categoriesResult.push(validCategories.map((category) => category._id));
  }

  return categoriesResult;
};

const validateCategories = async (categories) => {
  const validCategories = await Category.find({ _id: { $in: categories } });

  const invalidcategories = validCategories.filter(
    (category) => !categories.includes(category._id.toString())
  );

  if (validCategories.length < 1 || invalidcategories.length > 0) {
    throw new AppError(
      `Following category are not found: ${
        validCategories.length < 1
          ? categories.join(", ")
          : invalidcategories.join(", ")
      }`,
      404
    );
  }

  return validCategories.map((category) => category._id);
};

exports.createSkills = createSkills;
exports.getSkills = getSkills;
exports.getSkillsByCategories = getSkillsByCategories;
exports.getSkill = getSkill;
exports.updateSkill = updateSkill;
exports.deleteSkill = deleteSkill;
exports.deleteSkills = deleteSkills;
