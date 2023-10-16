const mongoose = require("mongoose");

const skillsSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, "Please provide a valid skill title."],
  },
  categories: [
    {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Categories",
      required: [
        true,
        "Please provide a category to which this skill refers to.",
      ],
    },
  ],
});

const Skills = mongoose.model("Skills", skillsSchema);

module.exports = Skills;
