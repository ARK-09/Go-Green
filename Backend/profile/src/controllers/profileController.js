const Profile = require("../models/profiles");
const User = require("../models/user");
const Skill = require("../models/skills");
const {
  catchAsync,
  AppError,
  Password,
} = require("@ark-industries/gogreen-common");

const fieldsToExclude = [
  "password",
  "isActive",
  "invalidLoginCount",
  "phoneNo",
  "financeAllowed",
  "passwordChangedAt",
  "resetToken",
  "resetTokenExpireAt",
  "otp",
  "otpExpireAt",
  "blocked",
  "__v",
];

const getUserProfile = catchAsync(async (req, res, next) => {
  const userId = req.params.id;

  const userPopulate = {
    path: "user",
    select: `-${fieldsToExclude.join(" -")}`.replace("-phoneNo", "+phoneNo"),
  };

  if (req.currentUser.userType !== "admin") {
    userPopulate.match = {
      userType: {
        $in: ["client", "talent"],
      },
    };
  }

  const profile = await Profile.findOne({
    user: userId,
    active: { $ne: false },
  })
    .populate([
      { path: "projects", select: "-__v" },
      userPopulate,
      { path: "skills", select: "-__v" },
    ])
    .lean();

  if (!profile) {
    return next(
      new AppError(`No profile found with matching user id: ${userId}`, 404)
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
    req.currentUser._id.toString() === userId ||
    req.currentUser.userType === "admin";

  if (!isAllowed) {
    return next(
      new AppError("You don't have permission to perform this action.", 403)
    );
  }

  const user = await User.findById(userId);

  if (!user) {
    return next(new AppError(`No user find with the id: ${userId}.`, 404));
  }

  let profile = await Profile.findOne({
    user: userId,
    active: { $ne: false },
  }).populate([
    { path: "projects", select: "-__v" },
    { path: "skills", select: "-__v" },
    { path: "user", select: `-${fieldsToExclude.join(" -")}` },
  ]);

  if (!profile) {
    return next(
      new AppError(`No profile found with matching user id: ${userId}`, 404)
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

  if (skills) {
    const validSkills = await validateSkills(skills);
    profile.skills.push(...validSkills);
  }

  profile.dob = dob ? dob : profile.dob;
  profile.gender = gender ? gender : profile.gender;
  profile.address = address ? address : profile.address;

  if (location) {
    profile.location.coordinates = [
      location.coordinates[0],
      location.coordinates[1],
    ];
  }

  profile = await profile.save();

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
      new AppError(`No profile found with matching user id: ${userId}`, 404)
    );
  }

  profile.active = false;

  profile = await profile.save();

  res.status(204).json({
    status: "success",
    data: null,
  });
});

const searchProfiles = catchAsync(async (req, res, next) => {
  const { name, skills, location } = req.query;

  const profileQuery = {};
  const userQuery = {
    match: { userType: "talent" },
  };

  if (skills) {
    const skillsArray = skills.map((skill) => skill.trim());
    const skillIds = await Skill.find({
      title: {
        $in: skillsArray.map((skill) => new RegExp(skill, "i")),
      },
    })
      .select("_id")
      .lean();

    const skillIdsArray = skillIds.map((skill) => skill._id);

    if (skillIdsArray.length > 0) {
      profileQuery.skills = { $in: skillIdsArray };
    }
  }

  if (name) {
    userQuery.match = {
      name: { $regex: name, $options: "i" },
      userType: "talent",
    };
  }

  // if (req.query.location && req.query.location.coordinates) {
  //   const latitude = req.query.location.coordinates[0];
  //   const longitude = req.query.location.coordinates[1];

  //   const maxDistanceInKilometers = 500;

  //   profileQuery.location = {
  //     $near: {
  //       $geometry: {
  //         type: "Point",
  //         coordinates: [parseFloat(longitude), parseFloat(latitude)],
  //       },
  //       $maxDistance: maxDistanceInKilometers * 1000, // Convert kilometers to meters
  //     },
  //   };
  // }

  const profiles = await Profile.find(profileQuery)
    .sort({ createdDate: 1 })
    .populate([
      { path: "projects", select: "-__v" },
      { path: "skills", select: "-__v" },
      {
        path: "user",
        select: `-${fieldsToExclude.join(" -")}`,
        match: userQuery.match,
      },
    ])
    .lean();

  const talentProfiles = profiles.filter((profile) => profile.user !== null);

  res.status(200).json({
    status: "success",
    data: {
      profiles: talentProfiles,
    },
  });
});

const validateSkills = async (skills) => {
  const validSkills = await Skill.find({ _id: { $in: skills } });

  const invalidSkills = validSkills.filter(
    (skill) => !skills.includes(skill._id.toString())
  );

  if (validSkills.length < 1 || invalidSkills.length > 0) {
    throw new AppError(
      `Following skill are not found: ${
        validSkills.length < 1 ? skills.join(", ") : invalidSkills.join(", ")
      }`,
      404
    );
  }

  return validSkills;
};

exports.getUserProfile = getUserProfile;
exports.updateUserProfile = updateUserProfile;
exports.deleteUserProfile = deleteUserProfile;
exports.searchProfiles = searchProfiles;
