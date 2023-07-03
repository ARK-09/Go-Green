const mongoose = require("mongoose");

const messageSchema = new mongoose.Schema({
  text: {
    type: String,
    required: [true, "Message cant be an empty string."],
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
  roomId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide roomid for the message."],
  },
  senderId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, "Please provide valid sender id."],
  },
  status: {
    type: String,
    enum: {
      values: ["read", "deleted", "draft", "sent"],
      message:
        "Message status can be only one of these: 'read', 'deleted', 'draft', 'sent'",
    },
    default: "sent",
  },
  createdDate: {
    type: Date,
    default: Date.now,
  },
});

const Message = mongoose.model("Messages", messageSchema);

module.exports = Message;
