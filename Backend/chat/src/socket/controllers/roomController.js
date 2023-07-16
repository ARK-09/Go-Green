const mongoose = require("mongoose");
const { AppError } = require("@ark-industries/gogreen-common");
const catchAsyncSocketError = require("../util/catchAsyncSocketError");
const Room = require("../../models/room");
const Message = require("../../models/message");

const joinRoom = catchAsyncSocketError(async (socket) => {
  const roomId = socket.data.roomId;

  // if (!mongoose.Types.ObjectId.isValid(roomId)) {
  //   throw new AppError(`Invalid room id: ${roomId}`, 400);
  // }

  // const room = await Room.findById(roomId);

  // if (!room) {
  //   throw new AppError(`No room found with matching id: ${roomId}`, 404);
  // }

  // const currentUser = socket.payload.id;

  // const isRoomMember = room.members.some((member) => {
  //   member.userId.toString() === currentUser;
  // });

  // if (!isRoomMember) {
  //   throw new AppError(`You are not allowed to join this room.`, 403);
  // }

  socket.join(roomId);
  socket.emit("room:join", {
    status: "success",
    message: "Successfully joined the room",
    roomId,
  });
});

const sendMessage = catchAsyncSocketError(async (socket) => {
  const { roomId, message, attachments } = socket.data;
  const currentUser = socket.payload.id;

  // if (!mongoose.Types.ObjectId.isValid(roomId)) {
  //   throw new AppError(`Invalid room id: ${roomId}`, 400);
  // }

  // const room = await Room.findById(roomId);

  // if (!room) {
  //   throw new AppError(`No room found with matching id: ${roomId}`, 404);
  // }

  // const isRoomMember = room.members.some((member) => {
  //   member.userId.toString() === currentUser;
  // });

  // if (!isRoomMember) {
  //   throw new AppError(`You are not allowed to join this room.`, 403);
  // }

  // const messageDb = new Message();
  // message.text = message;
  // message.roomId = roomId;
  // message.senderId = currentUser;

  // if (attachments) {
  //   message.attachments.push(...attachments);
  // }

  // await messageDb.save();

  const response = {
    message: {
      message: message,
      attachments: attachments,
    },
    roomId: roomId,
    senderId: currentUser,
  };

  socket.to(roomId).emit("message:receive", response);
});

const deleteMessage = catchAsyncSocketError(async (socket) => {
  const { roomId, messageId } = socket.data;
  const currentUser = socket.payload.id;

  // if (!mongoose.Types.ObjectId.isValid(roomId)) {
  //   throw new AppError(`Invalid room id: ${roomId}`, 400);
  // }

  // const room = await Room.findById(roomId);

  // if (!room) {
  //   throw new AppError(`No room found with matching id: ${roomId}`, 404);
  // }

  // const isRoomMember = room.members.some((member) => {
  //   member.userId.toString() === currentUser;
  // });

  // if (!isRoomMember) {
  //   throw new AppError(`You are not allowed to join this room.`, 403);
  // }

  const message = await Message.findByIdAndUpdate(messageId, {
    status: "deleted",
  });

  if (!message) {
    throw new AppError(`No message found with id: ${messageId}`, 404);
  }

  const response = {
    messageId: messageId,
    roomId: roomId,
    senderId: currentUser,
  };

  socket.to(roomId).emit("message:delete", response);
});

const typingStart = catchAsyncSocketError(async (socket) => {
  const { roomId } = socket.data;
  socket.to(roomId).emit("typing:start", { roomId, userId: socket.payload.id });
});

const typingStop = catchAsyncSocketError(async (socket) => {
  const { roomId } = socket.data;
  socket.to(roomId).emit("typing:stop", { roomId, userId: socket.payload.id });
});

exports.joinRoom = joinRoom;
exports.sendMessage = sendMessage;
exports.deleteMessage = deleteMessage;
exports.typingStart = typingStart;
exports.typingStop = typingStop;
