const dotenv = require("dotenv");

dotenv.config({ path: __dirname + "./config.env" });

const mongoose = require("mongoose");
const app = require("./app");


mongoose.connect ()

const port = process.env.PORT || 4000;

app.listen(port, () => {
  console.log(`Listening on port: ${port}`);
});
