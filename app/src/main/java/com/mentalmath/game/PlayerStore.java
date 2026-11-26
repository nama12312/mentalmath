package com.mentalmath.game;

import android.content.Context;
import android.content.SharedPreferences;

/** Saves the player's skill, best score and rounds played on the phone. */
public final class PlayerStore {

    private static final String FILE = "player";
    private static final String KEY_SKILL = "skill";
    private static final String KEY_BEST = "best_score";
    private static final String KEY_GAMES = "games_played";

    private final SharedPreferences prefs;

    public PlayerStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public double getSkill() {
        return prefs.getFloat(KEY_SKILL, 1.0f);
    }

    public int getBestScore() {
        return prefs.getInt(KEY_BEST, 0);
    }

    public int getGamesPlayed() {
        return prefs.getInt(KEY_GAMES, 0);
    }

    /**
     * Saves the result of a finished round.
     *
     * @return true if this round set a new best score
     */
    public boolean saveRound(double newSkill, int score) {
        boolean newBest = score > getBestScore();
        SharedPreferences.Editor editor = prefs.edit()
                .putFloat(KEY_SKILL, (float) newSkill)
                .putInt(KEY_GAMES, getGamesPlayed() + 1);
        if (newBest) {
            editor.putInt(KEY_BEST, score);
        }
        editor.apply();
        return newBest;
    }
}
