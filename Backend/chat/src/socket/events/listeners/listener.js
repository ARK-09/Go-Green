const {
  joinRoom,
  sendMessage,
  deleteMessage,
  typingStart,
  typingStop,
} = require("../../controllers/roomController");
const SocketServer = require("../../socketServer");

const socket = new SocketServer().socket;

const init = () => {
  socket
    .on("room:join", (data) => {
      socket.data = data;
      joinRoom(socket);
    })
    .on("message:send", (data) => {
      socket.data = data;
      sendMessage(socket);
    })
    .on("message:delete", (data) => {
      socket.data = data;
      deleteMessage(socket);
    })
    .on("typing:start", (data) => {
      socket.data = data;
      typingStart(socket);
    })
    .on("typing:stop", (data) => {
      socket.data = data;
      typingStop(socket);
    });
};

exports.init = init;
