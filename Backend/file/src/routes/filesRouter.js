const express = require("express");
const { param } = require("express-validator");
const {
  requireAuth,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const currentUser = require("../middelwares/currentUser");
const FilesController = require("../controllers/fileController");
const { multer } = FilesController;

const JWT_KEY = process.env.JWT_KEY;

const uploadMiddleware = (req, res, next) => {
  multer.array("files", 5)(req, res, function (err) {
    if (err) {
      console.log(err);
      let errorMessage;
      if (
        err instanceof multer.MulterError &&
        err.code === "LIMIT_UNEXPECTED_FILE"
      ) {
        errorMessage =
          "Error while uploading files Maximum number of files allowed is 5";
      } else {
        errorMessage = "File upload failed!. Please try again.";
      }

      return res.status(500).json({
        status: "fail",
        message: errorMessage,
      });
    }
    next();
  });
};

const router = express.Router();

router
  .route("/")
  .post(
    requireAuth(JWT_KEY),
    currentUser,
    uploadMiddleware,
    FilesController.uploadFile
  );

router
  .route("/:id")
  .get(
    requireAuth(JWT_KEY),
    currentUser,
    param("id").isMongoId().withMessage("Please provide a valid id"),
    validateRequest,
    FilesController.getFile
  )
  .delete(
    requireAuth(JWT_KEY),
    currentUser,
    param("id").isMongoId().withMessage("Please provide a valid id"),
    validateRequest,
    FilesController.deleteFile
  );

module.exports = router;
