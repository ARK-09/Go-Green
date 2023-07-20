const {
  joinRoom,
  sendMessage,
  deleteMessage,
  typingStart,
  typingStop,
} = require("../../controllers/roomController");
const SocketServer = require("../../socketServer");
const { validatePayload } = require("../../util/validator");

const init = () => {
  SocketServer.getSocket()
    .onAny(async (...args) => {
      SocketServer.getSocket().data = args[1];
      validatePayload();
    })
    .on("room:join", joinRoom)
    .on("message:send", sendMessage)
    .on("message:delete", deleteMessage)
    .on("typing:start", typingStart)
    .on("typing:stop", typingStop);
};

exports.init = init;
