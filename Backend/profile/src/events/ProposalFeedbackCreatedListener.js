const { Listener } = require("@ark-industries/gogreen-common");
const RatingCalculator = require("../util/ratingCalculator");
const Proposal = require("../models/proposals");
const Profile = require("../models/profiles");
const Job = require("../models/jobs");

class ProposalFeedbackCreatedListener extends Listener {
  subject = "proposal:feedback-created";
  queueGroupName = "profile-service-queue-group";

  constructor(client) {
    super(client);
  }

  onMessage = async (data, message) => {
    if (data) {
      await Proposal.findByIdAndUpdate(data._id, { ...data });
      const job = await Job.findById(data.doc);

      const clientProfile = await Profile.findOne({ user: job.user });
      const talentProfile = await Profile.findOne({ user: data.user });

      const clientAdjustedRating = RatingCalculator.calculateRating(
        data.talentRating,
        clientProfile.rating
      );
      const talentAdjustedRating = RatingCalculator.calculateRating(
        data.clientRating,
        talentProfile.rating
      );

      clientProfile.rating = clientAdjustedRating;
      talentProfile.rating = talentAdjustedRating;

      await Promise.all([
        clientProfile.save({ validateBeforeSave: false }),
        talentProfile.save({ validateBeforeSave: false }),
      ])
        .then(() => {
          console.log("Ratting updated successfully");
        })
        .catch((err) => {
          console.log("Error updating rating", err);
        });

      message.ack();
    }
  };
}

module.exports = ProposalFeedbackCreatedListener;
