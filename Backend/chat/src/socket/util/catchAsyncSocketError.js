const catchAsyncSocketError = (fn) => {
  return (socket, next) => {
    fn(socket, next).catch((err) => {
      console.log(err);
      const error = {
        message: err.message || err.msg,
        status: `${err.status}`.startsWith("4") ? "fail" : "error",
      };
      socket.emit("error", error);
    });
  };
};

module.exports = catchAsyncSocketError;
