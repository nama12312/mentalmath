package com.mentalmath.game;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/** End-of-round summary with your score, stats and any level change. */
public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "score";
    public static final String EXTRA_CORRECT = "correct";
    public static final String EXTRA_ACCURACY = "accuracy";
    public static final String EXTRA_AVG_SECONDS = "avg_seconds";
    public static final String EXTRA_BEST_STREAK = "best_streak";
    public static final String EXTRA_OLD_LEVEL = "old_level";
    public static final String EXTRA_NEW_LEVEL = "new_level";
    public static final String EXTRA_NEW_BEST = "new_best";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent in = getIntent();
        int score = in.getIntExtra(EXTRA_SCORE, 0);
        int oldLevel = in.getIntExtra(EXTRA_OLD_LEVEL, 1);
        int newLevel = in.getIntExtra(EXTRA_NEW_LEVEL, 1);
        boolean newBest = in.getBooleanExtra(EXTRA_NEW_BEST, false);

        ((TextView) findViewById(R.id.statCorrect))
                .setText(String.valueOf(in.getIntExtra(EXTRA_CORRECT, 0)));
        ((TextView) findViewById(R.id.statAccuracy))
                .setText(getString(R.string.percent_value, in.getIntExtra(EXTRA_ACCURACY, 0)));
        ((TextView) findViewById(R.id.statAvgTime))
                .setText(getString(R.string.seconds_value, in.getDoubleExtra(EXTRA_AVG_SECONDS, 0)));
        ((TextView) findViewById(R.id.statStreak))
                .setText(String.valueOf(in.getIntExtra(EXTRA_BEST_STREAK, 0)));

        TextView levelChange = findViewById(R.id.levelChange);
        if (newLevel > oldLevel) {
            levelChange.setText(getString(R.string.level_up, oldLevel, newLevel));
            levelChange.setTextColor(ContextCompat.getColor(this, R.color.correct));
        } else if (newLevel < oldLevel) {
            levelChange.setText(getString(R.string.level_down, oldLevel, newLevel));
        } else {
            levelChange.setText(getString(R.string.level_same, newLevel));
        }

        countUp(findViewById(R.id.finalScore), score);

        View newBestBadge = findViewById(R.id.newBest);
        if (newBest && score > 0) {
            newBestBadge.setVisibility(View.VISIBLE);
            newBestBadge.setScaleX(0f);
            newBestBadge.setScaleY(0f);
            newBestBadge.animate().scaleX(1f).scaleY(1f).setStartDelay(900).setDuration(400)
                    .setInterpolator(new OvershootInterpolator(3f)).start();
        }

        findViewById(R.id.playAgainButton).setOnClickListener(v -> {
            startActivity(new Intent(this, GameActivity.class));
            finish();
        });
        findViewById(R.id.homeButton).setOnClickListener(v -> finish());
    }

    private void countUp(TextView view, int target) {
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(Math.min(1200, 300 + target));
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> view.setText(String.valueOf((int) a.getAnimatedValue())));
        animator.start();
    }
}
