const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const Room = require("../models/room");
const Message = require("../models/message");
const User = require("../models/user");

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

const createRoom = catchAsync(async (req, res, next) => {
  let { name, members } = req.body;

  members = Array.from(new Set(members));

  if (members.some((member) => member === req.currentUser.id)) {
    return next(new AppError("The user is already a member.", 401));
  }

  const validUser = await User.find({ _id: { $in: members }, isActive: true });

  const invalidMembers = members.filter((userId) => {
    return !validUser.some((user) => {
      return user.id.toString() === userId;
    });
  });

  if (validUser.length == 0 || invalidMembers.length > 0) {
    return next(
      new AppError(
        `The following users not exist: ${
          validUser.length == 0 ? members.join(", ") : invalidMembers.join(", ")
        }`,
        404
      )
    );
  }

  const room = await Room.create({
    name,
    members,
    ownerId: req.currentUser.id,
  });

  const roomPopulated = await room.populate(
    "members",
    `-${fieldsToExclude.join(" -")}`
  );

  roomPopulated.members = roomPopulated.members.map((member) => {
    member.id = member._id;
    delete member._id;
    return member;
  });

  res.status(201).json({
    status: "success",
    data: { room: roomPopulated },
  });
});

const getRooms = catchAsync(async (req, res, next) => {
  const { offset = 1, limit = 10 } = req.query;
  const currentUser = req.currentUser.id;

  const filterQuery = {
    ownerId: currentUser,
  };

  const totalRooms = await Room.countDocuments(filterQuery);

  const totalPages = Math.ceil(totalRooms / limit);
  const skip = (offset - 1) * limit;

  const rooms = await Room.find(filterQuery)
    .populate("members", `-${fieldsToExclude.join(" -")}`)
    .skip(skip)
    .limit(limit)
    .lean();

  const roomsRestul = rooms.map((room) => {
    room.members = room.members.map((member) => {
      member.id = member._id ? member._id : member.id;
      delete member._id;
      return member;
    });
    return room;
  });

  res.status(200).json({
    status: "success",
    totalRooms,
    totalPages,
    offset,
    data: { rooms: roomsRestul },
  });
});

const getRoom = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id: ${id}`, 200));
  }

  const isAllowed =
    room.members.some((member) => member === currentUser) ||
    room.ownerId.toString() === currentUser;

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const roomPopulated = await room.populate(
    "members",
    `-${fieldsToExclude.join(" -")}`
  );

  roomPopulated.members.forEach((member) => {
    member.id = member._id;
    delete member._id;
  });

  res.status(200).json({
    status: "success",
    data: {
      room: roomPopulated,
    },
  });
});

const getRoomMessages = catchAsync(async (req, res, next) => {
  const { offset = 1, limit = 10 } = req.query;
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id: ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member === currentUser) ||
    room.ownerId.toString() === currentUser;

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
  let { members } = req.body;
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  members = Array.from(new Set(members));

  if (members.some((member) => member === req.currentUser.id)) {
    return next(new AppError("The user is already a member.", 401));
  }

  const validUser = await User.find({ _id: { $in: members }, isActive: true });

  const invalidMembers = members.filter((userId) => {
    return !validUser.some((user) => {
      return user.id.toString() === userId;
    });
  });

  if (validUser.length == 0 || invalidMembers.length > 0) {
    return next(
      new AppError(
        `The following users not exist: ${
          validUser.length == 0 ? members.join(", ") : invalidMembers.join(", ")
        }`,
        404
      )
    );
  }

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member === currentUser) ||
    room.ownerId.toString() === currentUser;

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const membersParesent = members.filter((member) =>
    room.members.includes(member)
  );

  if (membersParesent.length > 0) {
    return next(
      new AppError(`The user is already a member: ${membersParesent}`, 401)
    );
  }

  room.members.push(...members);
  await room.save();

  await room.populate("members", `-${fieldsToExclude.join(" -")}`);

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

  const room = await Room.findById(id).populate(
    "members",
    `-${fieldsToExclude.join(" -")}`
  );

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member === currentUser) ||
    room.ownerId.toString() === currentUser;

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
  const { memberid } = req.params;
  const { id } = req.params;
  const currentUser = req.currentUser.id;

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member === currentUser) ||
    room.ownerId.toString() === currentUser;

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const member = room.members.find((member) => member === memberid);

  if (!member) {
    return next(new AppError(`No member found with id: ${memberid}`, 404));
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
    return next(new AppError(`No room found with id ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member === currentUser) ||
    room.ownerId.toString() === currentUser;

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const memberIndex = room.members.findIndex((member) => member === memberid);

  if (memberIndex === -1) {
    return next(new AppError(`No member found with id: ${memberid}`, 404));
  }

  room.members.splice(memberIndex, 1);
  await room.save();

  res.status(204).json({
    status: "success",
    data: null,
  });
});

exports.createRoom = createRoom;
exports.getRooms = getRooms;
exports.getRoom = getRoom;
exports.getRoomMessages = getRoomMessages;
exports.addRoomMembers = addRoomMembers;
exports.getRoomMembers = getRoomMembers;
exports.getRoomMember = getRoomMember;
exports.deleteRoomMember = deleteRoomMember;
