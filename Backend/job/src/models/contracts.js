const mongoose = require("mongoose");

const contractSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: [true, "Please provide a user id. Who initiated the contract."],
  },
  proposalId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Proposals",
    required: [true, "Please provide a valid proposal ID."],
  },
  startTime: {
    type: Date,
    required: [true, "Please provide a start time."],
    default: Date.now,
  },
  endTime: {
    type: Date,
  },
  amount: {
    type: Number,
    required: [true, "Please provide an amount."],
  },
  status: {
    type: String,
    enum: [
      "Draft",
      "Pending Approval",
      "In Progress",
      "Delivered",
      "Revision",
      "Payment Pending",
      "Completed",
      "Canceled",
    ],
    required: [true, "Please provide a valid status."],
    default: "Draft",
  },
});

const Contract = mongoose.model("Contracts", contractSchema);

module.exports = Contract;
