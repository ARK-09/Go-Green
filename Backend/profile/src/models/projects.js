const mongoose = require("mongoose");

const projectsSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, "Project must have a valid name."],
  },
  description: {
    type: String,
    required: [true, "Project must have a valid description."],
    maxlength: 350,
  },
  startDate: {
    type: Date,
    required: [true, "Project start date can't be empty"],
    default: Date.now,
  },
  endDate: {
    type: Date,
    required: [true, "Project end date can't be empty"],
  },
  attachments: [
    {
      id: {
        type: String,
      },
      mimeType: {
        type: String,
        enum: {
          values: [
            "image/jpeg",
            "image/png",
            "image/gif",
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
          ],
          message:
            "Invalid MIME type provided. Only the following MIME types are allowed: image/jpeg, image/png, image/gif, video/mp4, video/quicktime.",
        },
      },
      originalName: String,
      url: String,
      createdDate: { type: Date, default: Date.now },
      _id: false,
    },
  ],
  skills: [
    {
      id: mongoose.Schema.Types.ObjectId,
    },
  ],
  contractId: {
    type: mongoose.Schema.Types.ObjectId,
  },
});

const ProjectsModel = mongoose.model("Projects", projectsSchema);

module.exports = ProjectsModel;
