const express = require("express");
const profileRouter = require("./routes/profileRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/profiles", profileRouter);

module.exports = app;
