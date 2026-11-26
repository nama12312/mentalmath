package com.mentalmath.game.logic;

/** One quick mental-math question. Answers are always whole numbers of zero or more. */
public final class Question {
    public final String text;
    public final int answer;
    /** The difficulty level this question was made for (1 to {@link SkillTracker#MAX_LEVEL}). */
    public final int level;

    public Question(String text, int answer, int level) {
        this.text = text;
        this.answer = answer;
        this.level = level;
    }

    /** How many digits the answer has, so the game can check as soon as enough are typed. */
    public int answerLength() {
        return String.valueOf(answer).length();
    }

    @Override
    public String toString() {
        return text + " = " + answer;
    }
}
