class RatingCalculator {
  static calculateRating(newRating, currentRating) {
    const maxRating = 5;

    const averageRating = (currentRating + newRating) / 2;

    return Math.min(averageRating, maxRating).toFixed(2);
  }
}

module.exports = RatingCalculator;
