const mongoose = require("mongoose");

const proposalSchema = new mongoose.Schema({
  refId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [
      true,
      "Please provide a valid reference ID. (jobId or serviceId)",
    ],
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: [true, "Please provide a valid user ID."],
  },
  bidAmount: {
    type: Number,
    required: [true, "Please provide a bid amount."],
  },
  status: {
    type: String,
    enum: ["Draft", "Submitted", "Declined", "Accepted", "Withdraw", "Hired"],
    required: [true, "Please provide a valid status."],
    default: "Submitted",
  },
  coverLetter: {
    type: String,
    required: [true, "Please provide a cover letter."],
  },
  proposedDuration: {
    type: String,
    enum: [
      "Less than 1 month",
      "1 to 3 months",
      "3 to 6 months",
      "More than 6 months",
    ],
    required: [true, "Please provide a valid proposed duration."],
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
      createdDate: { type: Date, default: Date.now },
      _id: false,
    },
  ],
  clientFeedback: {
    type: String,
    default: null,
  },
  clientRating: {
    type: Number,
    min: 0,
    max: 5,
    default: 0.0,
  },
  talentFeedback: {
    type: String,
    default: null,
  },
  talentRating: {
    type: Number,
    min: 0,
    max: 5,
    default: 0.0,
  },
  type: {
    type: String,
    enum: ["job", "service"],
    required: [true, "Please provide a valid type."],
  },
});

const Proposal = mongoose.model("Proposals", proposalSchema);

module.exports = Proposal;
