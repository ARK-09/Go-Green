const mongoose = require("mongoose");
const app = require("./app");

const connectionString = process.env.MONGO_URI;

mongoose.connect(connectionString).then(() => {
  console.log("DB connection successful!");
});

const port = parseInt(process.env.PORT) || 4000;

const server = app.listen(port, () => {
  console.log(`Listening at port:${port}`);
});

process.on("uncaughtException", (err) => {
  console.log(err.name, err.message);
  console.log("uncaughtException! Shutting down...");

  server.close(() => {
    process.exit(1);
  });
});

process.on("unhandledRejection", (err) => {
  console.log(err.name, err.message, err.stack);
  console.log("unhandledRejection! Shutting down...");

  server.close(() => {
    process.exit(1);
  });
});
