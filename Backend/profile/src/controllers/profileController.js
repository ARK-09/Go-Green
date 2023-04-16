const Profile = require("../models/profiles");

const getUserProfile = async (req, res, next) => {
  const userId = req.params.id;

  const profile = await Profile.findOne({ userId });

  if (!profile) {
  }
};

exports.getUserProfile = getUserProfile;
