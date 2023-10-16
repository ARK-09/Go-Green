const catchAsyncSocketError = (fn) => {
  return function (data) {
    fn(this, data).catch((err) => {
      const error = {
        message: err.message || err.msg,
        name: err.name,
        status: `${err.status}`.startsWith("4") ? "fail" : "error",
      };

      this?.emit("error", error);
    });
  };
};

module.exports = catchAsyncSocketError;
