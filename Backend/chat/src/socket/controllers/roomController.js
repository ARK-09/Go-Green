const { AppError } = require("@ark-industries/gogreen-common");
const validator = require("validator");
const catchAsyncSocketError = require("../util/catchAsyncSocketError");
const Room = require("../../models/room");
const Message = require("../../models/message");
const extractValidProperties = require("../../util/extractValidProperties");

const fieldsToExclude = [
  "password",
  "isActive",
  "invalidLoginCount",
  "phoneNo",
  "financeAllowed",
  "passwordChangedAt",
  "resetToken",
  "resetTokenExpireAt",
  "otp",
  "otpExpireAt",
  "blocked",
  "__v",
];

const joinRoom = catchAsyncSocketError(async (socket, data) => {
  const { roomId } = JSON.parse(data);

  console.log(JSON.parse(data));

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const currentUser = socket.currentUser._id;

  const isRoomMember =
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

  if (!isRoomMember) {
    throw new AppError(`You are not allowed to join this room.`, 403);
  }

  await Message.updateMany(
    { room: roomId, status: "sent", sender: { $ne: currentUser } },
    { status: "read" }
  );

  socket.join(roomId);
  socket.emit("room:joined", {
    status: "success",
    message: "Successfully joined the room",
    roomId,
  });
});

const sendMessage = catchAsyncSocketError(async (socket, data) => {
  const { roomId, message } = JSON.parse(data);

  let { attachments } = JSON.parse(data);

  if (attachments) {
    attachments = extractValidProperties(attachments, [
      "id",
      "mimeType",
      "originalName",
      "createdDate",
    ]);
  }

  const currentUser = socket.currentUser._id;

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const isRoomMember = room.members.some(
    (member) => member.toString() === currentUser
  );
  const isOwner = room.owner.toString() === currentUser;

  if (!isRoomMember && !isOwner) {
    throw new AppError(`You are not allowed to join this room.`, 403);
  }

  if (typeof message != "string") {
    throw new AppError("Please provide a valid message.", 400);
  }

  const messageObject = {
    text: message,
    room: roomId,
    sender: currentUser,
  };

  if (attachments && Array.isArray(attachments)) {
    messageObject.attachments = attachments;
  }

  const messageDb = await Message.create(messageObject);

  room.lastMessage = messageDb._id;
  await room.save();

  const populatedMessage = await Message.populate(messageDb, [
    { path: "sender", select: `-${fieldsToExclude.join(" -")}` },
  ]);

  const response = {
    ...populatedMessage.toObject(),
  };

  socket.to(roomId).emit("message:receive", response);
  socket.emit("message:sent", response);

  // if (isRoomMember) {
  //   const membersToNotify = room.members.filter(
  //     (member) => member.toString() != currentUser
  //   );

  //   membersToNotify.forEach((member) => {
  //     socket
  //       .to(`notifications/users/${member}/messages`)
  //       .emit("notification", response);
  //   });

  //   socket
  //     .to(`notifications/users/${room.owner}/messages`)
  //     .emit("notification", response);
  // } else {
  //   room.members.forEach((member) => {
  //     socket
  //       .to(`notifications/users/${member}/messages`)
  //       .emit("notification", response);
  //   });
  // }
});

const deleteMessage = catchAsyncSocketError(async (socket, data) => {
  const { roomId, messageId } = JSON.parse(data);
  const currentUser = socket.currentUser._id;

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const isRoomMember =
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

  if (!isRoomMember) {
    throw new AppError(`You are not allowed to join this room.`, 403);
  }

  const message = await Message.findById(messageId);

  if (!message) {
    throw new AppError(`No message found with id: ${messageId}`, 404);
  }

  if (message.sender.toString() !== currentUser) {
    throw new AppError("You'r not allowed to perform this action", 403);
  }

  await Message.findByIdAndDelete(messageId);

  const response = {
    messageId: messageId,
    room: roomId,
    sender: currentUser,
  };

  socket.to(roomId).emit("message:delete", response);
  socket.emit("message:deleted", response);
});

const typingStart = catchAsyncSocketError(async (socket, data) => {
  const { roomId } = JSON.parse(data);

  socket.to(roomId).emit("typing:start", {
    roomId,
    userId: socket.currentUser._id,
  });
});

const typingStop = catchAsyncSocketError(async (socket, data) => {
  const { roomId } = JSON.parse(data);

  socket
    .to(roomId)
    .emit("typing:stop", { roomId, userId: socket.currentUser._id });
});

exports.joinRoom = joinRoom;
exports.sendMessage = sendMessage;
exports.deleteMessage = deleteMessage;
exports.typingStart = typingStart;
exports.typingStop = typingStop;
