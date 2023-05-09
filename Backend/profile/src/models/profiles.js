const mongoose = require("mongoose");

const profilesSchema = new mongoose.Schema({
  about: {
    type: String,
    default: "",
  },
  languages: {
    type: [
      {
        name: String,
        experience: {
          type: String,
          enum: {
            values: ["Beginner", "Intermediate", "Fluent", "Native speaker"],
            message:
              'Experience can have only these values ["Beginner", "Intermediate", "Fluent", "Native speaker"]',
          },
        },
      },
    ],
    required: [true, "Please add at least one language."],
    default: [{ name: "English", experience: "Fluent" }],
  },
  dob: {
    type: Date,
    required: [true, "Date of birth can't be empty."],
    default: Date.now(),
  },
  ranking: {
    type: String,
    enum: {
      values: ["fresh_face", "rising_talent", "top_rated", "top_rated_plus"],
      message:
        "Ranking can have only these values ['rising_talent', 'top_rated', 'top_rated_plus']",
    },
    default: "fresh_face",
  },
  address: {
    type: String,
    default: "",
  },
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
