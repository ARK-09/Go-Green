const mongoose = require("mongoose");

const roomMemberSchema = new mongoose.Schema({
  roomId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide a valid room id."],
  },
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide a valid user id for the room."],
  },
});

const RoomMember = mongoose.model("RoomMembers", roomMemberSchema);

module.exports = RoomMember;
