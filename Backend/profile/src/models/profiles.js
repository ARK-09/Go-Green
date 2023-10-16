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
    type: {
      type: String,
      default: "Point",
      enum: {
        values: ["Point"],
        message:
          "Please provide location type as 'Point' no other values are acceptable.",
      },
    },
    coordinates: {
      type: [Number],
      index: "2dsphere",
      required: [
        true,
        "Please provide job location coordinates as [longitude, latitude].",
      ],
      validate: {
        validator: (coords) => coords.length === 2,
        message:
          "Coordinates should contain exactly two elements: [longitude, latitude].",
      },
      default: [0.0, 0.0],
    },
  },
  projects: [
    {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Projects",
    },
  ],
  skills: [
    {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Skills",
    },
  ],
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
  },
  rating: {
    type: Number,
    default: 5.0,
  },
  active: {
    type: Boolean,
    default: true,
  },
  createdDate: {
    type: Date,
    default: Date.now,
  },
});

profilesSchema.index({ "location.coordinates": "2dsphere" });

const ProfilesModel = mongoose.model("Profiles", profilesSchema);

module.exports = ProfilesModel;
