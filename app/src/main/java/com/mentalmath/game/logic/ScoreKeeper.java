package com.mentalmath.game.logic;

/** Points, streaks and stats for one round. */
public final class ScoreKeeper {

    private int score;
    private int streak;
    private int bestStreak;
    private int correct;
    private int answered;
    private double totalCorrectSeconds;

    /** Combo multiplier: x1, then +1 for every 5 in a row, up to x4. */
    public int multiplier() {
        return Math.min(4, 1 + streak / 5);
    }

    /**
     * Records an answer and returns the points earned (0 for a wrong answer).
     */
    public int record(boolean isCorrect, double seconds, int level) {
        answered++;
        if (!isCorrect) {
            streak = 0;
            return 0;
        }
        correct++;
        totalCorrectSeconds += seconds;
        streak++;
        bestStreak = Math.max(bestStreak, streak);

        int base = 10 * level;
        double target = SkillTracker.targetSeconds(level);
        int speedBonus = seconds < target ? (int) Math.round(base * (1 - seconds / target)) : 0;
        int points = (base + speedBonus) * multiplier();
        score += points;
        return points;
    }

    public int getScore() { return score; }
    public int getStreak() { return streak; }
    public int getBestStreak() { return bestStreak; }
    public int getCorrect() { return correct; }
    public int getAnswered() { return answered; }

    public int accuracyPercent() {
        return answered == 0 ? 0 : Math.round(100f * correct / answered);
    }

    public double averageSeconds() {
        return correct == 0 ? 0 : totalCorrectSeconds / correct;
    }
}
