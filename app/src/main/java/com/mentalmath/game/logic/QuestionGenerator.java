package com.mentalmath.game.logic;

import java.util.Random;

/**
 * Makes questions for a given level. Every level is designed to be solved in your head in a few
 * seconds: higher levels use bigger numbers and trickier operations, never long working-out.
 */
public final class QuestionGenerator {

    private static final String PLUS = " + ";
    private static final String MINUS = " − ";
    private static final String TIMES = " × ";
    private static final String DIVIDE = " ÷ ";

    private final Random random;
    private String lastText = "";

    public QuestionGenerator() {
        this(new Random());
    }

    public QuestionGenerator(Random random) {
        this.random = random;
    }

    public Question next(int level) {
        int clamped = Math.max(1, Math.min(SkillTracker.MAX_LEVEL, level));
        Question q;
        int tries = 0;
        do {
            q = make(clamped);
            tries++;
        } while (q.text.equals(lastText) && tries < 5);
        lastText = q.text;
        return q;
    }

    private Question make(int level) {
        switch (level) {
            case 1: return pick(level, add(1, 9, 1, 9), sub(1, 10));
            case 2: return pick(level, add(2, 12, 2, 9), sub(5, 20), times(2, 5, 1, 10), times(10, 10, 1, 10));
            case 3: return pick(level, add(11, 89, 2, 9), subTwoByOne(), times(2, 10, 2, 10));
            case 4: return pick(level, addTens(), subTens(), divide(2, 10, 2, 10), times(2, 10, 2, 10));
            case 5: return pick(level, add(11, 89, 11, 89), subTwoDigit(), times(2, 12, 2, 12), doubleIt(), halveIt());
            case 6: return pick(level, times(11, 39, 2, 9), squares(11, 15), addRoundToThree(), divide(2, 12, 2, 12));
            case 7: return pick(level, percent(), times(21, 99, 2, 9), divide(3, 9, 11, 25), subTwoDigit());
            case 8: return pick(level, timesEleven(), squares(11, 20), addThreeByTwo(), timesTwentyFive(), percent());
            case 9: return pick(level, timesRoundTens(), timesTeens(), addThreeByTwo(), divide(11, 15, 2, 12), squares(11, 25));
            default: return pick(level, timesTeens(), timesRoundTens(), squares(15, 30), percent(), timesEleven(), subThreeByTwo());
        }
    }

    // ---- building blocks -------------------------------------------------------------------

    /** Picks one of several candidate questions at random. */
    private Question pick(int level, Question... options) {
        Question chosen = options[random.nextInt(options.length)];
        return new Question(chosen.text, chosen.answer, level);
    }

    private int between(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private Question add(int aMin, int aMax, int bMin, int bMax) {
        int a = between(aMin, aMax);
        int b = between(bMin, bMax);
        return random.nextBoolean() ? q(a + PLUS + b, a + b) : q(b + PLUS + a, a + b);
    }

    /** Subtraction whose answer stays at zero or above, with the larger number up to {@code max}. */
    private Question sub(int minTop, int max) {
        int a = between(minTop, max);
        int b = between(1, a);
        return q(a + MINUS + b, a - b);
    }

    private Question subTwoByOne() {
        int a = between(12, 99);
        int b = between(2, 9);
        return q(a + MINUS + b, a - b);
    }

    private Question subTwoDigit() {
        int a = between(30, 99);
        int b = between(11, a - 1);
        return q(a + MINUS + b, a - b);
    }

    private Question subThreeByTwo() {
        int a = between(120, 999);
        int b = between(11, 99);
        return q(a + MINUS + b, a - b);
    }

    /** e.g. 47 + 30 */
    private Question addTens() {
        int a = between(11, 89);
        int b = between(1, 9 - a / 10) * 10;
        return q(a + PLUS + b, a + b);
    }

    /** e.g. 83 − 40 */
    private Question subTens() {
        int a = between(21, 99);
        int b = between(1, a / 10) * 10;
        return q(a + MINUS + b, a - b);
    }

    /** e.g. 340 + 70 */
    private Question addRoundToThree() {
        int a = between(11, 89) * 10;
        int b = between(2, 9) * 10;
        return q(a + PLUS + b, a + b);
    }

    private Question addThreeByTwo() {
        int a = between(101, 899);
        int b = between(11, 99);
        return q(a + PLUS + b, a + b);
    }

    private Question times(int aMin, int aMax, int bMin, int bMax) {
        int a = between(aMin, aMax);
        int b = between(bMin, bMax);
        return random.nextBoolean() ? q(a + TIMES + b, a * b) : q(b + TIMES + a, a * b);
    }

    /** Division that always comes out exact: (a × b) ÷ b. */
    private Question divide(int bMin, int bMax, int answerMin, int answerMax) {
        int b = between(bMin, bMax);
        int answer = between(answerMin, answerMax);
        return q((answer * b) + DIVIDE + b, answer);
    }

    private Question squares(int min, int max) {
        int a = between(min, max);
        return q(a + "²", a * a);
    }

    private Question doubleIt() {
        int a = between(13, 99);
        return q("Double " + a, a * 2);
    }

    private Question halveIt() {
        int a = between(7, 99) * 2;
        return q("Half of " + a, a / 2);
    }

    /** 10%, 20%, 25%, 50% or 75% of a number that gives a whole answer. */
    private Question percent() {
        int[] percents = {10, 20, 25, 50, 75};
        int p = percents[random.nextInt(percents.length)];
        int step = (p == 25 || p == 75) ? 4 : (p == 50 ? 2 : 10);
        int base = between(2, 40) * step;
        return q(p + "% of " + base, base * p / 100);
    }

    private Question timesEleven() {
        int a = between(12, 99);
        return q(a + TIMES + "11", a * 11);
    }

    private Question timesTwentyFive() {
        int a = between(2, 24);
        return q(a + TIMES + "25", a * 25);
    }

    /** e.g. 34 × 20 */
    private Question timesRoundTens() {
        int a = between(12, 49);
        int b = between(2, 9) * 10;
        return q(a + TIMES + b, a * b);
    }

    /** e.g. 13 × 14 */
    private Question timesTeens() {
        int a = between(11, 19);
        int b = between(11, 19);
        return q(a + TIMES + b, a * b);
    }

    private static Question q(String text, int answer) {
        return new Question(text, answer, 0);
    }
}
