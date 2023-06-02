const Message = require("../models/message");
const { catchAsync, AppError } = require("@ark-industries/gogreen-common");

const updateMessage = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const message = await Message.findById(id);

  if (!message) {
    next(new AppError(`No message found with id: ${id}`, 204));
  }

  const { text } = req.body;

  message.text = text || message.text;
  await message.save();

  res.status(200).json({
    status: "success",
    data: {
      message,
    },
  });
});

const deleteMessage = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const message = await Message.findByIdAndDelete(id);

  if (!message) {
    next(new AppError(`No message found with id: ${id}`, 204));
  }

  res.status(200).json({
    status: "success",
    data: null,
  });
});

exports.updateMessage = updateMessage;
exports.deleteMessage = deleteMessage;
