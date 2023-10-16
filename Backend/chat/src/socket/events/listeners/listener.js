const {
  joinRoom,
  sendMessage,
  deleteMessage,
  typingStart,
  typingStop,
} = require("../../controllers/roomController");
const { validatePayload } = require("../../util/validator");

const init = (socket) => {
  socket
    .onAny(async (...args) => {
      validatePayload(socket, args[1]);
    })
    .on("room:join", joinRoom)
    .on("message:send", sendMessage)
    .on("message:delete", deleteMessage)
    .on("typing:start", typingStart)
    .on("typing:stop", typingStop);
};

exports.init = init;
