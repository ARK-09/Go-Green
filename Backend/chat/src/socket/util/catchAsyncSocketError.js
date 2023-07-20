const SocketServer = require("../socketServer");

const catchAsyncSocketError = (fn) => {
  return () => {
    fn().catch((err) => {
      const error = {
        message: err.message || err.msg,
        name: err.name,
        status: `${err.status}`.startsWith("4") ? "fail" : "error",
      };

      SocketServer.getSocket().emit("error", error);
    });
  };
};

module.exports = catchAsyncSocketError;
