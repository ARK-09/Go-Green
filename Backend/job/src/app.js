const rateLimit = require("express-rate-limit");
const helmet = require("helmet");
const mongoSanitize = require("express-mongo-sanitize");
const hpp = require("hpp");
const bodyParser = require("body-parser");
const compression = require("compression");
const cors = require("cors");
const express = require("express");
const {
  globalErrorController,
  AppError,
} = require("@ark-industries/gogreen-common");

const contractRouter = require("./routes/contractsRouter");
const jobRouter = require("./routes/jobsRouter");
const proposalRouter = require("./routes/proposalsRouter");
const categoriesRouter = require("./routes/categoriesRouter");
const skillsRouter = require("./routes/skillsRouter");

const app = express();

app.enable("trust proxy");

const bodySize =
  typeof process.env.BODY_SIZE === "string" ? process.env.BODY_SIZE : undefined;
app.use(express.json({ limit: bodySize }));
app.use(bodyParser.urlencoded({ extended: true, limit: bodySize }));

const allowedOrigins = process.env.ALLOWED_ORIGINS.split(",");
app.use(
  cors({
    origin: allowedOrigins,
  })
);

app.options("*", cors());
app.use(helmet());

const requestRateWindow =
  parseInt(process.env.REQUEST_RATE_WINDOW) || undefined;
const maxRequest = parseInt(process.env.MAX_REQUEST) || 100;
const limiter = rateLimit({
  max: maxRequest,
  windowMs: requestRateWindow,
  message: "Too many requests from this IP, please try again in an hour!",
});
app.use("/api", limiter);

app.use(mongoSanitize());

app.use(hpp());

app.use(compression());

app.use("/api/v1/contracts", contractRouter);
app.use("/api/v1/jobs", jobRouter);
app.use("/api/v1/proposals", proposalRouter);
app.use("/api/v1/categories", categoriesRouter);
app.use("/api/v1/skills", skillsRouter);

app.all("*", (req, res, next) => {
  return next(
    new AppError(`Can't find path: ${req.originalUrl} on this server`, 404)
  );
});

app.use(globalErrorController);

module.exports = app;
