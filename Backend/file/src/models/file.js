const mongoose = require("mongoose");

const fileSchema = new mongoose.Schema({
  key: {
    type: String,
    required: [true, "Please provide a valid file ID."],
  },
  originalName: {
    type: String,
    required: [true, "Please provide a valid image name."],
  },
  mimeType: {
    type: String,
    validate: {
      validator: function (value) {
        const mimeTypeRegex =
          /^(text\/plain)$|^(image\/(jpeg|jpg|png|gif))$|^(video\/(mp4|mpeg|quicktime))$|^(audio\/(mp3|wav))$|^(application\/(pdf|msword|vnd\.openxmlformats-officedocument\.wordprocessingml\.document))$/;
        return mimeTypeRegex.test(value);
      },
      message:
        "Invalid MIME type provided. Only the following MIME types are allowed: text/plain image/jpeg, image/png, image/gif, video/mp4, video/mpeg, video/quicktime, audio/mp3, audio/wav, application/pdf, application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    },
  },
  createdDate: {
    type: Date,
    default: Date.now,
  },
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide a valid user id."],
  },
  purpose: {
    type: String,
    enum: {
      values: ["profile", "job", "proposal", "Services", "message"],
    },
  },
});

fileSchema.virtual("url").set(function (url) {
  this.set(url);
});

const File = mongoose.model("Files", fileSchema);

module.exports = File;
