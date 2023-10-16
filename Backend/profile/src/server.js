// require("dotenv").config({ path: "./config.env" });

const mongoose = require("mongoose");
const app = require("./app");
const { natsWrapper } = require("@ark-industries/gogreen-common");
const UserCreatedListener = require("./events/userCreatedListener");
const UserUpdatedListener = require("./events/userUpdatedListener");
const UserDeletedListener = require("./events/userDeletedListener");
const UserForgetPasswordListener = require("./events/userForgetPasswordListener");
const UserResetPasswordListener = require("./events/userResetPasswordListener");
const SkillCreatedListener = require("./events/skillCreatedListener");
const SkillUpdatedListener = require("./events/skillUpdatedListener");
const SillDeletedListener = require("./events/skillDeletedListener");
const CategoryCreatedListener = require("./events/categoryCreatedListener");
const CategoryUpdatedListener = require("./events/categoryUpdatedListener");
const CategoryDeletedListener = require("./events/categoryDeletedListener");
const JobCreatedListener = require("./events/jobCreatedListener");
const JobUpdatedListener = require("./events/jobUpdatedListener");
const JobDeletedListener = require("./events/jobDeletedListener");
const ProposalCreatedListener = require("./events/proposalCreatedListener");
const ProposalUpdatedListener = require("./events/proposalUpdatedListener");
const ProposalFeedbackCreatedListener = require("./events/ProposalFeedbackCreatedListener");

natsWrapper
  .connect(
    process.env.NATS_CLUSTER_ID,
    process.env.NATS_CLIENT_ID,
    process.env.NATS_URL
  )
  .then(() => {
    new UserCreatedListener(natsWrapper.client).listen();
    new UserUpdatedListener(natsWrapper.client).listen();
    new UserDeletedListener(natsWrapper.client).listen();
    new UserForgetPasswordListener(natsWrapper.client).listen();
    new UserResetPasswordListener(natsWrapper.client).listen();
    new SkillCreatedListener(natsWrapper.client).listen();
    new SkillUpdatedListener(natsWrapper.client).listen();
    new SillDeletedListener(natsWrapper.client).listen();
    new CategoryCreatedListener(natsWrapper.client).listen();
    new CategoryUpdatedListener(natsWrapper.client).listen();
    new CategoryDeletedListener(natsWrapper.client).listen();
    new JobCreatedListener(natsWrapper.client).listen();
    new JobUpdatedListener(natsWrapper.client).listen();
    new JobDeletedListener(natsWrapper.client).listen();
    new ProposalCreatedListener(natsWrapper.client).listen();
    new ProposalUpdatedListener(natsWrapper.client).listen();
    new ProposalFeedbackCreatedListener(natsWrapper.client).listen();

    natsWrapper.client.on("close", () => {
      console.log("NATAS connection closed!");
      process.exit();
    });

    process.on("SIGINT", () => natsWrapper.client.close());
    process.on("SIGTERM", () => natsWrapper.client.close());
  });

const connectionString = process.env.MONGO_URI;
mongoose.connect(connectionString).then(() => {
  console.log("DB connection successful!");
});

const port = parseInt(process.env.PORT) || 4001;

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
