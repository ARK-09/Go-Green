const mongoose = require("mongoose");

const profilesSchema = new mongoose.Schema({
  about: String,
  languages: [
    {
      name: String,
      experience: String,
      required: true,
    },
  ],
  dob: {
    type: Date,
    required: true,
  },
  ranking: {
    type: String,
    enum: {
      values: ["rising_talent", "top_rated", "top_rated_plus"],
      message:
        "Ranking can have only these values ['rising_talent', 'top_rated', 'top_rated_plus']",
    },
  },
  address: String,
  location: [
    {
      type: {
        type: String,
        default: "Point",
        enum: ["Point"],
      },
      coordinates: [Number],
      required: true,
    },
  ],
  projects: [
    {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Projects",
    },
  ],
  skills: [String],
  userId: mongoose.Schema.Types.ObjectId,
});

const ProfilesModel = mongoose.Model("Profiles", profilesSchema);

module.exports = ProfilesModel;
