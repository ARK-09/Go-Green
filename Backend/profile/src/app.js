const express = require("express");
const profileRouter = require("./routes/profileRouter");
const projectRouter = require("./routes/projectRouter");

const app = express();

app.use(express.json());

app.use("/api/v1/profiles", profileRouter);
app.use("/api/v1/profiles/:id/projects", projectRouter);

module.exports = app;
