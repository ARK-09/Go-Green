const { AppError } = require("@ark-industries/gogreen-common");
const validator = require("validator");
const catchAsyncSocketError = require("../util/catchAsyncSocketError");
const Room = require("../../models/room");
const Message = require("../../models/message");
const SocketServer = require("../socketServer");
const currentUser = require("../../middelwares/currentUser");

const joinRoom = catchAsyncSocketError(async () => {
  const socket = SocketServer.getSocket();

  const { roomId } = socket.data;

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const currentUser = socket.currentUser.id;

  const isRoomMember =
    room.members.some((member) => {
      member === currentUser;
    }) || room.ownerId.toString() === currentUser;

  if (!isRoomMember) {
    throw new AppError(`You are not allowed to join this room.`, 403);
  }

  socket.join(roomId);
  socket.emit("room:join", {
    status: "success",
    message: "Successfully joined the room",
    roomId,
  });
});

const sendMessage = catchAsyncSocketError(async () => {
  const socket = SocketServer.getSocket();

  const { roomId, message, attachments } = socket.data;

  const currentUser = socket.currentUser.id;

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const isRoomMember =
    room.members.some((member) => {
      member === currentUser;
    }) || room.ownerId.toString() === currentUser;

  if (!isRoomMember) {
    throw new AppError(`You are not allowed to join this room.`, 403);
  }

  if (typeof message != "string") {
    throw new AppError("Please provide a valid message.", 400);
  }

  const messageDb = new Message();
  message.text = message;
  message.roomId = roomId;
  message.senderId = currentUser;

  if (attachments) {
    message.attachments.push(...attachments);
  }

  await messageDb.save();

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

const deleteMessage = catchAsyncSocketError(async () => {
  const socket = SocketServer.getSocket();

  const { roomId, messageId } = socket.data;
  const currentUser = socket.currentUser.id;

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const isRoomMember =
    room.members.some((member) => {
      member === currentUser;
    }) || room.ownerId.toString() === currentUser;

  if (!isRoomMember) {
    throw new AppError(`You are not allowed to join this room.`, 403);
  }

  const message = await Message.findById(messageId);

  if (!message) {
    throw new AppError(`No message found with id: ${messageId}`, 404);
  }

  if (message.senderId.toString() !== currentUser) {
    throw new AppError("You'r not allowed to perform this action", 403);
  }

  const response = {
    messageId: messageId,
    roomId: roomId,
    senderId: currentUser,
  };

  socket.to(roomId).emit("message:delete", response);
});

const typingStart = catchAsyncSocketError(async () => {
  const socket = SocketServer.getSocket();

  const { roomId } = socket.data;

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const isRoomMember =
    room.members.some((member) => {
      member === currentUser;
    }) || room.ownerId.toString() === currentUser;

  if (!isRoomMember) {
    throw new AppError(`You are not allowed to perform this action.`, 403);
  }

  socket
    .to(roomId)
    .emit("typing:start", { roomId, userId: socket.currentUser.id });
});

const typingStop = catchAsyncSocketError(async () => {
  const { roomId } = socket.data;

  if (!validator.isMongoId(roomId)) {
    throw new AppError(`Invalid room id: ${roomId}`, 400);
  }

  const room = await Room.findById(roomId);

  if (!room) {
    throw new AppError(`No room found with matching id: ${roomId}`, 404);
  }

  const isRoomMember =
    room.members.some((member) => {
      member === currentUser;
    }) || room.ownerId.toString() === currentUser;

  if (!isRoomMember) {
    throw new AppError(`You are not allowed to perform this action.`, 403);
  }

  socket
    .to(roomId)
    .emit("typing:stop", { roomId, userId: socket.currentUser.id });
});

exports.joinRoom = joinRoom;
exports.sendMessage = sendMessage;
exports.deleteMessage = deleteMessage;
exports.typingStart = typingStart;
exports.typingStop = typingStop;
