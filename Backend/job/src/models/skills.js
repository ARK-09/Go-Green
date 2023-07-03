const mongoose = require("mongoose");

const skillsSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, "Please provide a valid skill title."],
  },
});

const Skills = mongoose.model("Skills", skillsSchema);

module.exports = Skills;
