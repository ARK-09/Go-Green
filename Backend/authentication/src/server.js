const dotenv = require("dotenv");

dotenv.config({ path: __dirname + "./config.env" });

const mongoose = require("mongoose");
const app = require("./app");


// mongoose.connect ()

const port = process.env.PORT || 4000;

const server = app.listen(port, () => {
  var host = server.address().address
  var port = server.address().port
  
  console.log(`Listening at http://${host}:${port}`)
});
