const mongoose = require("mongoose");

const categoriesSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, "Please provide a valid category title."],
  },
});

const Categories = mongoose.model("Categories", categoriesSchema);

module.exports = Categories;
