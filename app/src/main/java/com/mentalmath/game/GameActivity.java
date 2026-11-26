package com.mentalmath.game;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.mentalmath.game.logic.Question;
import com.mentalmath.game.logic.QuestionGenerator;
import com.mentalmath.game.logic.ScoreKeeper;
import com.mentalmath.game.logic.SkillTracker;

/**
 * One 60-second round. The answer is checked as soon as you have typed as many digits as the
 * answer has, so there is no "submit" button to slow you down.
 */
public class GameActivity extends AppCompatActivity {

    public static final long ROUND_MILLIS = 60_000;
    private static final long LOW_TIME_MILLIS = 10_000;
    private static final long TICK_MILLIS = 30;
    private static final long CORRECT_PAUSE_MILLIS = 220;
    private static final long WRONG_PAUSE_MILLIS = 750;
    private static final int MAX_DIGITS = 5;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final QuestionGenerator generator = new QuestionGenerator();
    private final ScoreKeeper scoreKeeper = new ScoreKeeper();
    private final StringBuilder typed = new StringBuilder();

    private SkillTracker tracker;
    private double startingSkill;
    private Question question;

    private long remainingMillis = ROUND_MILLIS;
    private long lastTickAt;
    private long questionStartedAt;
    private boolean running;      // the clock is ticking
    private boolean inputLocked = true;
    private boolean finished;
    private boolean lowTimeShown;

    private ProgressBar timerBar;
    private TextView scoreText;
    private TextView streakText;
    private TextView levelBadge;
    private TextView multiplierText;
    private TextView questionText;
    private TextView answerText;
    private TextView pointsPopup;
    private View questionCard;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            long now = SystemClock.elapsedRealtime();
            remainingMillis -= now - lastTickAt;
            lastTickAt = now;
            if (remainingMillis <= 0) {
                remainingMillis = 0;
                updateTimerBar();
                endRound();
                return;
            }
            updateTimerBar();
            handler.postDelayed(this, TICK_MILLIS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        timerBar = findViewById(R.id.timerBar);
        scoreText = findViewById(R.id.scoreText);
        streakText = findViewById(R.id.streakText);
        levelBadge = findViewById(R.id.levelBadge);
        multiplierText = findViewById(R.id.multiplierText);
        questionText = findViewById(R.id.questionText);
        answerText = findViewById(R.id.answerText);
        pointsPopup = findViewById(R.id.pointsPopup);
        questionCard = findViewById(R.id.questionCard);

        startingSkill = new PlayerStore(this).getSkill();
        tracker = new SkillTracker(startingSkill);
        updateLevelBadge();

        int[] digitKeys = {R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
                R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9};
        for (int digit = 0; digit < digitKeys.length; digit++) {
            final char c = (char) ('0' + digit);
            findViewById(digitKeys[digit]).setOnClickListener(v -> {
                tap(v);
                onDigit(c);
            });
        }
        findViewById(R.id.keyBackspace).setOnClickListener(v -> {
            tap(v);
            if (!inputLocked && typed.length() > 0) {
                typed.setLength(typed.length() - 1);
                showTyped();
            }
        });
        findViewById(R.id.keyClear).setOnClickListener(v -> {
            tap(v);
            if (!inputLocked) {
                typed.setLength(0);
                showTyped();
            }
        });

        runCountdown();
    }

    // ---- round flow -------------------------------------------------------------------------

    /** "3, 2, 1, GO!" before the clock starts. */
    private void runCountdown() {
        String[] steps = {"3", "2", "1", "GO!"};
        answerText.setText("");
        for (int i = 0; i < steps.length; i++) {
            final String label = steps[i];
            final boolean last = i == steps.length - 1;
            handler.postDelayed(() -> {
                questionText.setText(label);
                questionText.setTextColor(ContextCompat.getColor(this,
                        last ? R.color.correct : R.color.text_primary));
                questionText.setScaleX(1.8f);
                questionText.setScaleY(1.8f);
                questionText.setAlpha(0f);
                questionText.animate().scaleX(1f).scaleY(1f).alpha(1f)
                        .setDuration(350).setInterpolator(new OvershootInterpolator()).start();
                if (last) {
                    handler.postDelayed(this::startRound, 450);
                }
            }, i * 600L);
        }
    }

    private void startRound() {
        if (finished) return;
        questionText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        nextQuestion();
        // If the app went to the background during the countdown, onResume starts the clock.
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            resumeClock();
        }
    }

    private void resumeClock() {
        if (running || finished) return;
        running = true;
        lastTickAt = SystemClock.elapsedRealtime();
        handler.post(tick);
    }

    private void pauseClock() {
        if (!running) return;
        running = false;
        long now = SystemClock.elapsedRealtime();
        remainingMillis -= now - lastTickAt;
        handler.removeCallbacks(tick);
    }

    private void nextQuestion() {
        question = generator.next(tracker.getLevel());
        typed.setLength(0);
        questionText.setText(question.text);
        answerText.setTextColor(ContextCompat.getColor(this, R.color.accent_light));
        showTyped();

        questionCard.setTranslationX(80f);
        questionCard.setAlpha(0.3f);
        questionCard.animate().translationX(0f).alpha(1f).setDuration(140)
                .setInterpolator(new DecelerateInterpolator()).start();

        questionStartedAt = SystemClock.elapsedRealtime();
        inputLocked = false;
    }

    private void onDigit(char c) {
        if (inputLocked || typed.length() >= MAX_DIGITS) return;
        typed.append(c);
        showTyped();
        if (typed.length() >= question.answerLength()) {
            checkAnswer();
        }
    }

    private void checkAnswer() {
        inputLocked = true;
        double seconds = (SystemClock.elapsedRealtime() - questionStartedAt) / 1000.0;
        boolean correct = Integer.parseInt(typed.toString()) == question.answer;

        int oldLevel = tracker.getLevel();
        tracker.record(correct, seconds, question.level);
        int points = scoreKeeper.record(correct, seconds, question.level);

        if (correct) {
            showCorrect(points);
        } else {
            showWrong();
        }
        if (tracker.getLevel() != oldLevel) {
            updateLevelBadge();
            bump(levelBadge);
        }
        handler.postDelayed(() -> {
            if (!finished) nextQuestion();
        }, correct ? CORRECT_PAUSE_MILLIS : WRONG_PAUSE_MILLIS);
    }

    private void endRound() {
        if (finished) return;
        finished = true;
        running = false;
        inputLocked = true;
        handler.removeCallbacksAndMessages(null);

        int oldLevel = new SkillTracker(startingSkill).getLevel();
        boolean newBest = new PlayerStore(this).saveRound(tracker.getSkill(), scoreKeeper.getScore());

        Intent intent = new Intent(this, ResultActivity.class)
                .putExtra(ResultActivity.EXTRA_SCORE, scoreKeeper.getScore())
                .putExtra(ResultActivity.EXTRA_CORRECT, scoreKeeper.getCorrect())
                .putExtra(ResultActivity.EXTRA_ACCURACY, scoreKeeper.accuracyPercent())
                .putExtra(ResultActivity.EXTRA_AVG_SECONDS, scoreKeeper.averageSeconds())
                .putExtra(ResultActivity.EXTRA_BEST_STREAK, scoreKeeper.getBestStreak())
                .putExtra(ResultActivity.EXTRA_OLD_LEVEL, oldLevel)
                .putExtra(ResultActivity.EXTRA_NEW_LEVEL, tracker.getLevel())
                .putExtra(ResultActivity.EXTRA_NEW_BEST, newBest);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // ---- feedback ---------------------------------------------------------------------------

    private void showCorrect(int points) {
        answerText.setTextColor(ContextCompat.getColor(this, R.color.correct));
        scoreText.setText(String.valueOf(scoreKeeper.getScore()));
        streakText.setText(String.valueOf(scoreKeeper.getStreak()));
        bump(scoreText);
        bump(streakText);
        updateMultiplier();

        questionCard.animate().scaleX(1.04f).scaleY(1.04f).setDuration(90).withEndAction(() ->
                questionCard.animate().scaleX(1f).scaleY(1f).setDuration(90).start()).start();

        pointsPopup.setText(getString(R.string.points_popup, points));
        pointsPopup.setTranslationY(40f);
        pointsPopup.setAlpha(1f);
        pointsPopup.animate().translationY(-40f).alpha(0f).setDuration(650)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private void showWrong() {
        answerText.setTextColor(ContextCompat.getColor(this, R.color.wrong));
        answerText.setText(getString(R.string.correct_answer, question.answer));
        streakText.setText("0");
        updateMultiplier();

        int feedback = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                ? HapticFeedbackConstants.REJECT
                : HapticFeedbackConstants.LONG_PRESS;
        questionCard.performHapticFeedback(feedback);

        questionCard.animate().cancel();
        questionCard.setTranslationX(0f);
        shake(questionCard, 0);
    }

    /** A quick side-to-side wobble. */
    private void shake(View view, int step) {
        float[] offsets = {-24f, 20f, -14f, 8f, 0f};
        if (step >= offsets.length) return;
        view.animate().translationX(offsets[step]).setDuration(45)
                .withEndAction(() -> shake(view, step + 1)).start();
    }

    private void bump(View view) {
        view.animate().scaleX(1.25f).scaleY(1.25f).setDuration(90).withEndAction(() ->
                view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
    }

    private void tap(View key) {
        key.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void showTyped() {
        answerText.setText(typed.length() == 0 ? "?" : typed.toString());
    }

    private void updateMultiplier() {
        int m = scoreKeeper.multiplier();
        if (m > 1) {
            boolean changed = !multiplierText.getText().toString()
                    .equals(getString(R.string.multiplier, m));
            multiplierText.setText(getString(R.string.multiplier, m));
            multiplierText.setVisibility(View.VISIBLE);
            if (changed) bump(multiplierText);
        } else {
            multiplierText.setVisibility(View.INVISIBLE);
            multiplierText.setText("");
        }
    }

    private void updateLevelBadge() {
        levelBadge.setText(getString(R.string.level_n, tracker.getLevel()));
    }

    private void updateTimerBar() {
        timerBar.setProgress((int) (1000 * remainingMillis / ROUND_MILLIS));
        if (!lowTimeShown && remainingMillis <= LOW_TIME_MILLIS) {
            lowTimeShown = true;
            timerBar.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.wrong)));
        }
    }

    // ---- lifecycle --------------------------------------------------------------------------

    @Override
    protected void onPause() {
        super.onPause();
        pauseClock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only restart the clock if the countdown already finished and a question is showing.
        if (question != null && !finished) {
            resumeClock();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
