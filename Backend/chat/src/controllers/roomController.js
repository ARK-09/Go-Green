const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const Room = require("../models/room");
const Message = require("../models/message");

const createRoom = catchAsync(async (req, res, next) => {
  const { name, members } = req.body;

  const room = await Room.create({
    name,
    members,
  });

  res.status(201).json({
    status: "success",
    data: { room },
  });
});

const getRooms = catchAsync(async (req, res, next) => {
  const { offset = 1, limit = 10 } = req.query;
  const currentUser = req.currentUser.id;

  const filterQuery = {
    "members.userId": currentUser,
  };

  const totalRooms = await Room.countDocuments(filterQuery);

  const totalPages = Math.ceil(totalRooms / limit);
  const skip = (offset - 1) * limit;

  const rooms = await Room.find(filterQuery).skip(skip).limit(limit).lean();

  res.status(200).json({
    status: "success",
    totalRooms,
    totalPages,
    offset,
    data: { rooms },
  });
});

const getRoom = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id: ${id}`, 200));
  }

  const isAllowed = room.members.some(
    (member) => member.userId.toString() === currentUser
  );

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  res.status(200).json({
    status: "success",
    data: {
      room,
    },
  });
});

const createRoomMessage = catchAsync(async (req, res, next) => {
  const { text, attachments } = req.body;
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id: ${id}`, 204));
  }

  const isAllowed = room.members.some(
    (member) => member.userId.toString() === currentUser
  );

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const message = new Message();
  message.text = text;
  message.roomId = id;
  message.senderId = currentUser;

  if (attachments) {
    message.attachments.push(...attachments);
  }

  await message.save();

  res.status(200).json({
    status: "success",
    data: {
      message,
    },
  });
});

const getRoomMessages = catchAsync(async (req, res, next) => {
  const { offset = 1, limit = 10 } = req.query;
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id: ${id}`, 204));
  }

  const isAllowed = room.members.some(
    (member) => member.userId.toString() === currentUser
  );

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const filterQuery = {
    roomId: id,
  };

  const totalMessages = await Message.countDocuments(filterQuery);
  const totalPages = Math.ceil(totalMessages / limit);
  const skip = (offset - 1) * limit;

  const messages = await Message.find(filterQuery)
    .skip(skip)
    .limit(limit)
    .lean();

  res.status(200).json({
    status: "success",
    totalMessages,
    totalPages,
    offset,
    data: { messages },
  });
});

const addRoomMembers = catchAsync(async (req, res, next) => {
  const { members } = req.body;
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 204));
  }

  const isAllowed = room.members.some(
    (member) => member.userId.toString() === currentUser
  );

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  room.members.push(...members);
  await room.save();

  res.status(200).json({
    status: "success",
    data: {
      room,
    },
  });
});

const getRoomMembers = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 204));
  }

  const isAllowed = room.members.some(
    (member) => member.userId.toString() === currentUser
  );

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  res.status(200).json({
    status: "success",
    data: {
      members: room.members,
    },
  });
});

const getRoomMember = catchAsync(async (req, res, next) => {
  const { memberid } = req.body;
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 204));
  }

  const isAllowed = room.members.some(
    (member) => member.userId.toString() === currentUser
  );

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const member = room.members.find(
    (member) => member._id.toString() === memberid
  );

  if (!member) {
    return next(new AppError(`No member found with id: ${memberid}`, 204));
  }

  res.status(200).json({
    status: "success",
    data: {
      member,
    },
  });
});

const deleteRoomMember = catchAsync(async (req, res, next) => {
  const { id, memberid } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 204));
  }

  const isAllowed = room.members.some(
    (member) => member.userId.toString() === currentUser
  );

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const memberIndex = room.members.findIndex(
    (member) => member._id.toString() === memberid
  );

  if (memberIndex === -1) {
    return next(new AppError(`No member found with id: ${memberid}`, 204));
  }

  room.members.splice(memberIndex, 1);
  await room.save();

  res.status(200).json({
    status: "success",
    data: null,
  });
});

exports.createRoom = createRoom;
exports.getRooms = getRooms;
exports.getRoom = getRoom;
exports.createRoomMessage = createRoomMessage;
exports.getRoomMessages = getRoomMessages;
exports.addRoomMembers = addRoomMembers;
exports.getRoomMembers = getRoomMembers;
exports.getRoomMember = getRoomMember;
exports.deleteRoomMember = deleteRoomMember;
