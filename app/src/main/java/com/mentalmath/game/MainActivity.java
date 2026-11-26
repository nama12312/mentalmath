package com.mentalmath.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mentalmath.game.logic.SkillTracker;

/** Home screen: shows your level and best score, and starts a round. */
public class MainActivity extends AppCompatActivity {

    private TextView levelNumber;
    private ProgressBar levelProgress;
    private TextView levelProgressText;
    private TextView bestScore;
    private TextView gamesPlayed;
    private View levelRing;
    private View playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        levelNumber = findViewById(R.id.levelNumber);
        levelProgress = findViewById(R.id.levelProgress);
        levelProgressText = findViewById(R.id.levelProgressText);
        bestScore = findViewById(R.id.bestScore);
        gamesPlayed = findViewById(R.id.gamesPlayed);
        levelRing = findViewById(R.id.levelRing);
        playButton = findViewById(R.id.playButton);

        playButton.setOnClickListener(v -> {
            v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                startActivity(new Intent(this, GameActivity.class));
            }).start();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        playIntro();
    }

    private void refresh() {
        PlayerStore store = new PlayerStore(this);
        SkillTracker tracker = new SkillTracker(store.getSkill());
        int level = tracker.getLevel();

        levelNumber.setText(String.valueOf(level));
        bestScore.setText(String.valueOf(store.getBestScore()));
        gamesPlayed.setText(String.valueOf(store.getGamesPlayed()));

        int percent = (int) Math.round(tracker.getProgress() * 100);
        levelProgress.setProgress(percent, true);
        if (level >= SkillTracker.MAX_LEVEL) {
            levelProgressText.setText(R.string.max_level);
        } else {
            levelProgressText.setText(getString(R.string.next_level_progress, percent, level + 1));
        }
    }

    private void playIntro() {
        levelRing.setScaleX(0.6f);
        levelRing.setScaleY(0.6f);
        levelRing.setAlpha(0f);
        levelRing.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();

        playButton.setTranslationY(60f);
        playButton.setAlpha(0f);
        playButton.animate().translationY(0f).alpha(1f).setStartDelay(150).setDuration(400).start();
    }
}
