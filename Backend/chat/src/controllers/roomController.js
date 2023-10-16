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

  if (members.some((member) => member === req.currentUser._id.toString())) {
    return next(new AppError("The user is already a member.", 401));
  }

  const validUser = await User.find({ _id: { $in: members }, isActive: true });

  const invalidMembers = members.filter((userId) => {
    return !validUser.some((user) => {
      return user._id.toString() === userId;
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
    owner: req.currentUser._id,
  });

  const roomPopulated = await room.populate([
    { path: "members", select: `-${fieldsToExclude.join(" -")}` },
    { path: "owner", select: `-${fieldsToExclude.join(" -")}` },
    { path: "lastMessage" },
  ]);

  res.status(201).json({
    status: "success",
    data: { room: roomPopulated },
  });
});

const getRooms = catchAsync(async (req, res, next) => {
  const { offset = 1, limit = 10 } = req.query;
  const currentUser = req.currentUser._id.toString();

  const filterQuery = {
    $or: [
      {
        owner: currentUser,
      },
      {
        members: { $in: [currentUser] },
      },
    ],
  };

  const totalRooms = await Room.countDocuments(filterQuery);

  const totalPages = Math.ceil(totalRooms / limit);
  const skip = (offset - 1) * limit;

  const rooms = await Room.find(filterQuery)
    .populate([
      { path: "members", select: `-${fieldsToExclude.join(" -")}` },
      { path: "owner", select: `-${fieldsToExclude.join(" -")}` },
      {
        path: "lastMessage",
        populate: { path: "sender", select: `-${fieldsToExclude.join(" -")}` },
      },
    ])
    .sort({ createdDate: 1 })
    .skip(skip)
    .limit(limit);

  const roomWithUnreadedMessages = await Promise.all(
    rooms.map(async (room) => {
      const unreadMessages = await Message.countDocuments({
        room: room._id,
        status: "sent",
        sender: { $ne: req.currentUser._id },
      });
      return { ...room.toObject(), unreadMessages };
    })
  );

  res.status(200).json({
    status: "success",
    totalRooms,
    totalPages,
    offset,
    data: { rooms: roomWithUnreadedMessages },
  });
});

const getRoom = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const currentUser = req.currentUser._id.toString();

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id: ${id}`, 200));
  }

  const isAllowed =
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const roomPopulated = await room.populate([
    { path: "members", select: `-${fieldsToExclude.join(" -")}` },
    { path: "owner", select: `-${fieldsToExclude.join(" -")}` },
    {
      path: "lastMessage",
      populate: { path: "sender", select: `-${fieldsToExclude.join(" -")}` },
    },
  ]);

  const unreadMessages = await Message.countDocuments({
    room: room._id,
    status: "sent",
    sender: { $ne: req.currentUser._id },
  });

  res.status(200).json({
    status: "success",
    data: {
      room: { ...roomPopulated.toObject(), unreadMessages },
    },
  });
});

const getRoomMessages = catchAsync(async (req, res, next) => {
  const { offset = 1, limit = 10 } = req.query;
  const { id } = req.params;
  const currentUser = req.currentUser._id.toString();

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room fo und with id: ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const filterQuery = {
    room: id,
  };

  const unreadMessages = await Message.countDocuments({
    room: id,
    status: "sent",
    sender: { $ne: currentUser },
  });
  const totalMessages = await Message.countDocuments(filterQuery);
  const totalPages = Math.ceil(totalMessages / limit);
  const skip = (offset - 1) * limit;

  const messages = await Message.find(filterQuery)
    .populate([{ path: "sender", select: `-${fieldsToExclude.join(" -")}` }])
    .sort({ _id: -1 })
    .skip(skip)
    .limit(limit)
    .lean();

  res.status(200).json({
    status: "success",
    totalMessages,
    totalPages,
    unreadMessages,
    offset,
    data: { messages },
  });
});

const addRoomMembers = catchAsync(async (req, res, next) => {
  let { members } = req.body;
  const { id } = req.params;
  const currentUser = req.currentUser._id.toString();

  members = Array.from(new Set(members));

  const validUser = await User.find({ _id: { $in: members }, isActive: true });

  const invalidMembers = members.filter((userId) => {
    return !validUser.some((user) => {
      return user._id.toString() === userId;
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
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

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

  if (members.some((member) => member.toString() === room.owner)) {
    return next(
      new AppError(`The user is already a memeber: ${room.owner}`, 401)
    );
  }

  room.members.push(...members);
  await room.save();

  await room.populate([
    { path: "members", select: `-${fieldsToExclude.join(" -")}` },
    { path: "owner", select: `-${fieldsToExclude.join(" -")}` },
  ]);

  res.status(200).json({
    status: "success",
    data: {
      room,
    },
  });
});

const getRoomMembers = catchAsync(async (req, res, next) => {
  const { id } = req.params;
  const currentUser = req.currentUser._id.toString();

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

  if (!isAllowed) {
    return next(
      new AppError("You'r are not allowed to access this room.", 403)
    );
  }

  const roomPopulated = await room.populate([
    { path: "members", select: `-${fieldsToExclude.join(" -")}` },
    { path: "owner", select: `-${fieldsToExclude.join(" -")}` },
  ]);

  res.status(200).json({
    status: "success",
    data: {
      members: roomPopulated.members,
    },
  });
});

const getRoomMember = catchAsync(async (req, res, next) => {
  const { memberid } = req.params;
  const { id } = req.params;
  const currentUser = req.currentUser._id.toString();

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

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
  const currentUser = req.currentUser._id.toString();

  const room = await Room.findById(id);

  if (!room) {
    return next(new AppError(`No room found with id ${id}`, 404));
  }

  const isAllowed =
    room.members.some((member) => member.toString() === currentUser) ||
    room.owner.toString() === currentUser;

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
