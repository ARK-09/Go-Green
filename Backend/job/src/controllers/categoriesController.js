const Category = require("../models/categories");
const {
  catchAsync,
  AppError,
  natsWrapper,
} = require("@ark-industries/gogreen-common");

const CategoryCreatedPublisher = require("../events/categoryCreatedPublisher");
const CategoryUpdatedPublisher = require("../events/categoryUpdatedPublisher");
const CategoryDeletedPublisher = require("../events/categoryDeletedPublisher");

const createCategories = catchAsync(async (req, res, next) => {
  const { categories } = req.body;
  const categoriesObjectsArray = categories.map((title) => {
    return { title };
  });

  const categoriesDb = await Category.create(categoriesObjectsArray);

  await new CategoryCreatedPublisher(natsWrapper.client)
    .publish(categoriesDb)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: {
      categories: categoriesDb,
    },
  });
});

const getCategories = catchAsync(async (req, res, next) => {
  const categories = await Category.find({});

  res.status(200).json({
    status: "success",
    data: {
      categories,
    },
  });
});

const getCategory = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const category = await Category.findById(id);

  if (!category) {
    return next(new AppError(`No category found with matching id: ${id}`, 404));
  }

  res.status(200).json({
    status: "success",
    data: {
      category,
    },
  });
});

const updateCategory = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const { title } = req.body;

  const category = await Category.findByIdAndUpdate(
    id,
    { title },
    { new: true }
  );

  if (!category) {
    return next(new AppError(`No category found with id: ${id}`, 404));
  }

  await new CategoryUpdatedPublisher(natsWrapper.client)
    .publish(category)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: category,
  });
});

const deleteCategory = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const category = await Category.findById(id);

  if (!category) {
    return next(new AppError(`No category found with matching id: ${id}`, 404));
  }

  await Category.findByIdAndDelete(id);
  await new CategoryDeletedPublisher(natsWrapper.client)
    .publish(id)
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: null,
  });
});

const deleteCategories = catchAsync(async (req, res, next) => {
  await Category.deleteMany({});

  await new CategoryDeletedPublisher(natsWrapper.client)
    .publish({ all: true })
    .catch(() => {
      return next(new AppError("Something went wrong...", 500));
    });

  res.status(200).json({
    status: "success",
    data: null,
  });
});

exports.createCategories = createCategories;
exports.getCategories = getCategories;
exports.getCategory = getCategory;
exports.updateCategory = updateCategory;
exports.deleteCategory = deleteCategory;
exports.deleteCategories = deleteCategories;
