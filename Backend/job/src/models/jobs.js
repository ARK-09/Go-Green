const mongoose = require("mongoose");

const jobsSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, "Please provide a valid job title."],
  },
  description: {
    type: String,
    required: [true, "Please provide a valid job description."],
  },
  categories: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide a valid category id."],
  },
  budget: {
    type: Number,
    required: [true, "Please provide a valid job budget."],
  },
  status: {
    type: String,
    required: [true, "Please provide a valid job status."],
    enum: {
      values: [
        "Draft",
        "Open",
        "In Review",
        "Assigned",
        "Completed",
        "Canceled",
        "Disputed",
      ],
      message:
        "Status must be one of: Draft, Open, In Review, Assigned, Completed, Canceled, Disputed.",
    },
    default: "Draft",
  },
  expactedDuration: {
    type: String,
    required: [
      true,
      "Please provide a valid expected duration. Valid options: 'Less than 1 month', '1 to 3 months', '3 to 6 months', 'More than 6 months'.",
    ],
    default: "Less than 1 month",
  },
  paymentType: {
    type: String,
    required: [true, "Payment type must be one of: 'hourly', 'fixed'."],
  },
  attachments: [
    {
      id: mongoose.Schema.Types.ObjectId,
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
    },
  ],
  skills: [
    {
      id: mongoose.Schema.Types.ObjectId,
      ref: "Skills",
    },
  ],
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide a valid user ID."],
  },
});

const Jobs = mongoose.model("Jobs", jobsSchema);

module.exports = Jobs;
