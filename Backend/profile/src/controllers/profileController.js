const Profile = require("../models/profiles");
const { catchAsync, AppError } = require("@ark-industries/gogreen-common");

const getUserProfile = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  const profile = await Profile.findOne({
    userId: userId,
    active: { $ne: false },
  })
    .populate("projects")
    .lean();

  if (!profile) {
    return next(
      new AppError(`No profile found with matching user id: ${userId}`),
      204
    );
  }

  res.status(200).json({
    status: "success",
    data: { profile },
  });
});

const updateUserProfile = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  const isAllowed =
    req.currentUser.id === userId.id || req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  let profile = await Profile.findOne({ userId, active: { $ne: false } });

  if (!profile) {
    return next(
      new AppError(`No profile found with matching user id: ${userId}`),
      204
    );
  }

  const { about, languages, dob, gender, address, location, skills } = req.body;

  profile.about = about ? about : profile.about;

  if (languages) {
    if (Array.isArray(languages)) {
      profile.languages.push(...languages);
    } else if (typeof languages === "object") {
      profile.languages.push(languages);
    }
  }

  profile.dob = dob ? dob : profile.dob;
  profile.gender = gender ? gender : profile.gender;
  profile.address = address ? address : profile.address;
  profile.location = location ? location : profile.location;
  profile.skills = skills ? skills : profile.skills;

  profile = await profile.save();

  await profile.populate("projects");

  res.status(200).json({
    status: "success",
    data: { profile },
  });
});

const deleteUserProfile = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  let profile = await Profile.findOne({ userId, active: { $ne: false } });

  if (!profile) {
    return next(
      new AppError(`No profile found with matching user id: ${userId}`),
      204
    );
  }

  profile.active = false;

  profile = await profile.save();

  res.status(204).json({
    status: "success",
    data: { profile: null },
  });
});

exports.getUserProfile = getUserProfile;
exports.updateUserProfile = updateUserProfile;
exports.deleteUserProfile = deleteUserProfile;
