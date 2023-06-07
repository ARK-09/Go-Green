const mongoose = require("mongoose");

const roomSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, "Please provide a valid room name."],
  },
  members: [
    {
      userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
        required: true,
      },
      memberId: {
        type: mongoose.Schema.Types.ObjectId,
        default: mongoose.Types.ObjectId,
        required: true,
      },
    },
  ],
});

const Room = mongoose.model("Rooms", roomSchema);

module.exports = Room;
