const express = require("express");
const currentUser = require("../middelwares/currentUser");
const {
  requireAuth,
  validateRequest,
} = require("@ark-industries/gogreen-common");
const ContractController = require("../controllers/contractsControllers");

const JWT_KEY = process.env.JWT_KEY;

const router = express.Router({ mergeParams: true });

router
  .route("/")
  .post(
    check("proposalid")
      .isMongoId()
      .withMessage("Invalid proposal ID. Please provide a valid MongoDB ID."),
    check("amount")
      .isInt({ min: 5 })
      .withMessage("Amount field is required with min value 5."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ContractController.createContract
  )
  .get(requireAuth(JWT_KEY), currentUser, ContractController.getContracts);

router
  .route("/:id")
  .get(
    param("id")
      .isMongoId()
      .withMessage("Invalid contract ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ContractController.getContract
  )
  .patch(
    param("id")
      .isMongoId()
      .withMessage("Invalid contract ID. Please provide a valid MongoDB ID."),
    check("amount")
      .isInt({ min: 5 })
      .withMessage("Amount field is required with min value 5."),
    check("status")
      .notEmpty()
      .withMessage("Status field is required.")
      .isIn("Revision", "Completed", "Canceled")
      .withMessage(
        "Status can be only one of these 'Revision', 'Completed', 'Canceled'"
      ),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ContractController.updateContract
  )
  .delete(
    param("id")
      .isMongoId()
      .withMessage("Invalid contract ID. Please provide a valid MongoDB ID."),
    validateRequest,
    requireAuth(JWT_KEY),
    currentUser,
    ContractController.deleteContract
  );

module.exports = router;
