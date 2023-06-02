const mongoose = require("mongoose");

const roomSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, "Please provide a valid room name."],
  },
});

const Room = mongoose.model("Rooms", roomSchema);

module.exports = Room;
