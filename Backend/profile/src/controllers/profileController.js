const Profile = require("../models/profiles");

const getUserProfile = async (req, res, next) => {
  const userId = req.params.id;

  const profile = await Profile.findOne({ userId });

  if (!profile) {
  }

  res.status(200).json({
    message: "Welcome",
  });
};

exports.getUserProfile = getUserProfile;
