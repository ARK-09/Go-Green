const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const Room = require("../models/room");

const createRoom = catchAsync(async (req, res, next) => {
  const { name, members } = req.body;

  const room = await Room;
});

exports.createRoom = createRoom;
