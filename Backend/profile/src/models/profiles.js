const mongoose = require("mongoose");

const profilesSchema = new mongoose.Schema({
  about: String,
  languages: {
    type: [
      {
        name: String,
        experience: String,
      },
    ],
    required: [true, "Please add at least one language."],
  },
  dob: {
    type: Date,
    required: [true, "Date of birth can't be empty."],
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
  location: {
    type: [
      {
        type: {
          type: String,
          default: "Point",
          enum: ["Point"],
        },
        coordinates: [Number],
      },
    ],
    required: [true, "Please provide your location."],
  },
  projects: [
    {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Projects",
    },
  ],
  skills: [String],
  userId: mongoose.Schema.Types.ObjectId,
  active: {
    type: Boolean,
    default: true,
  },
});

const ProfilesModel = mongoose.model("Profiles", profilesSchema);

module.exports = ProfilesModel;
