package com.mentalmath.game.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Test;

public class QuestionGeneratorTest {

    @Test
    public void everyLevelGivesSmallWholeAnswers() {
        QuestionGenerator generator = new QuestionGenerator(new Random(42));
        for (int level = 1; level <= SkillTracker.MAX_LEVEL; level++) {
            for (int i = 0; i < 2000; i++) {
                Question q = generator.next(level);
                assertEquals(level, q.level);
                assertTrue(q + " should not be negative", q.answer >= 0);
                assertTrue(q + " is too long to do in your head", q.answerLength() <= 4);
            }
        }
    }

    @Test
    public void fastCorrectAnswersRaiseTheLevel() {
        SkillTracker tracker = new SkillTracker(1.0);
        for (int i = 0; i < 10; i++) {
            tracker.record(true, 1.0, tracker.getLevel());
        }
        assertTrue(tracker.getLevel() >= 2);
    }

    @Test
    public void mistakesLowerTheSkillButNeverBelowOne() {
        SkillTracker tracker = new SkillTracker(1.5);
        for (int i = 0; i < 10; i++) {
            tracker.record(false, 1.0, 1);
        }
        assertEquals(1.0, tracker.getSkill(), 0.0001);
    }

    @Test
    public void streaksGrowTheMultiplier() {
        ScoreKeeper keeper = new ScoreKeeper();
        for (int i = 0; i < 5; i++) {
            keeper.record(true, 10, 1);
        }
        assertEquals(2, keeper.multiplier());
        keeper.record(false, 1, 1);
        assertEquals(1, keeper.multiplier());
    }
}
