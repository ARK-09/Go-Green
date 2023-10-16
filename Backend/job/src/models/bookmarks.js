const mongoose = require("mongoose");

const BookmarkSchema = new mongoose.Schema({
  doc: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide a valid doc."],
  },
  type: {
    type: String,
    required: [
      true,
      "Please provide a valid type it can be 'job', 'proposal', 'user'",
    ],
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: [true, "Please provide a valid user id."],
  },
});

BookmarkSchema.virtual("document", {
  ref: function () {
    switch (this.type) {
      case "Jobs":
        return "Jobs";
      case "proposal":
        return "Proposals";
      case "user":
        return "User";
      default:
        return null;
    }
  },
  localField: "doc",
  foreignField: "_id",
  justOne: true,
});

const Bookmarks = mongoose.model("Bookmarks", BookmarkSchema);

module.exports = Bookmarks;
