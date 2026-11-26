package com.mentalmath.game.logic;

/**
 * Tracks the player's skill as a number between 1 and {@link #MAX_LEVEL + 1}. The whole part is
 * the level; the fraction is progress toward the next level.
 *
 * <p>Fast correct answers push the skill up, slow ones nudge it up a little, and mistakes pull it
 * down. Because the game asks for a new question at the current level after every answer, the
 * difficulty adjusts live during a round.
 */
public final class SkillTracker {

    public static final int MAX_LEVEL = 10;
    private static final double MIN_SKILL = 1.0;
    private static final double MAX_SKILL = MAX_LEVEL + 0.99;

    private double skill;

    public SkillTracker(double startingSkill) {
        this.skill = clamp(startingSkill);
    }

    public double getSkill() {
        return skill;
    }

    public int getLevel() {
        return (int) Math.floor(skill);
    }

    /** Progress toward the next level, from 0.0 to just under 1.0. */
    public double getProgress() {
        return skill - Math.floor(skill);
    }

    /** Seconds a confident player at this level should need. Higher levels get a little longer. */
    public static double targetSeconds(int level) {
        return 2.5 + level * 0.35;
    }

    /**
     * Records an answer and returns how much the skill changed.
     *
     * @param correct whether the answer was right
     * @param seconds how long the player took
     * @param level   the level the question was made for
     */
    public double record(boolean correct, double seconds, int level) {
        double before = skill;
        if (correct) {
            double target = targetSeconds(level);
            double gain;
            if (seconds <= target * 0.6) {
                gain = 0.22;          // lightning fast
            } else if (seconds <= target) {
                gain = 0.15;          // on pace
            } else if (seconds <= target * 2) {
                gain = 0.06;          // got there
            } else {
                gain = 0.02;          // slow, but right
            }
            skill = clamp(skill + gain);
        } else {
            skill = clamp(skill - 0.3);
        }
        return skill - before;
    }

    private static double clamp(double value) {
        return Math.max(MIN_SKILL, Math.min(MAX_SKILL, value));
    }
}
