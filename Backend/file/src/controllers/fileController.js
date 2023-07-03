const { catchAsync, AppError } = require("@ark-industries/gogreen-common");
const crypto = require("crypto");
const Multer = require("multer");
const MulterS3 = require("multer-s3");
const {
  S3Client,
  GetObjectCommand,
  DeleteObjectCommand,
} = require("@aws-sdk/client-s3");
const { getSignedUrl } = require("@aws-sdk/s3-request-presigner");
const File = require("../models/file");

const s3Client = new S3Client({
  region: process.env.BUCKET_REGION,
  credentials: {
    accessKeyId: process.env.ACCESS_KEY,
    secretAccessKey: process.env.SECRET_ACCESS_KEY,
  },
});

const randomFileId = (bytes = 32) => crypto.randomBytes(bytes).toString("hex");

const multer = Multer({
  storage: MulterS3({
    s3: s3Client,
    bucket: process.env.BUCKET_NAME,
    contentType: MulterS3.AUTO_CONTENT_TYPE,
    key: (req, file, cb) => {
      cb(null, randomFileId());
    },
  }),
});

const uploadFile = catchAsync(async (req, res, next) => {
  if (!req.files) {
    return next(new AppError("Error: uploading files. Please try again.", 500));
  }

  let files = [];

  const fileCreationPromises = req.files.map(async (currentFile) => {
    const file = (
      await File.create({
        key: currentFile.key,
        originalName: currentFile.originalname,
        mimeType: currentFile.mimetype,
        userId: req.currentUser.id,
      })
    ).toObject();

    file.id = file._id;

    delete file.userId;
    delete file._id;
    delete file.key;
    delete file.__v;

    files.push(file);
  });

  await Promise.all(fileCreationPromises);

  res.status(200).json({
    status: "success",
    data: {
      files,
    },
  });
});

const getFile = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const file = await File.findById(id);

  if (!file) {
    return next(new AppError(`No file found with id: ${id}`, 404));
  }

  const param = {
    Bucket: process.env.BUCKET_NAME,
    Key: file.key,
  };

  const command = new GetObjectCommand(param);
  const url = await getSignedUrl(s3Client, command, { expiresIn: 3600 });

  const fileWithUrl = file.toJSON();

  fileWithUrl.url = url;
  fileWithUrl.id = fileWithUrl._id;

  delete fileWithUrl._id;
  delete fileWithUrl.key;
  delete fileWithUrl.__v;

  res.status(200).json({
    status: "success",
    data: {
      file: fileWithUrl,
    },
  });
});

const deleteFile = catchAsync(async (req, res, next) => {
  const { id } = req.params;

  const file = await File.findById(id);

  if (!file) {
    return next(new AppError(`No file found with id: ${id}`, 404));
  }

  console.log(req.currentUser.id, file.userId.toString());
  const isAllowed = req.currentUser.id === file.userId.toString();

  if (!isAllowed) {
    return next(new AppError("You'r not allowed to perform this action.", 403));
  }

  const param = {
    Bucket: process.env.BUCKET_NAME,
    Key: file.key,
  };

  const command = new DeleteObjectCommand(param);
  await s3Client.send(command);

  await File.findByIdAndDelete(id);

  res.status(204).json({
    status: "success",
    data: null,
  });
});

module.exports.uploadFile = uploadFile;
module.exports.getFile = getFile;
module.exports.deleteFile = deleteFile;
module.exports.multer = multer;
