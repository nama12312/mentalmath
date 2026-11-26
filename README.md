# Mental Math

A fast, game-like Android app for getting better at mental arithmetic. Written in Java.

## How it plays

- **60-second rounds.** Answer as many questions as you can before the timer runs out.
- **No submit button.** The answer is checked as soon as you've typed enough digits, so you stay in the flow.
- **Adapts to you.** Fast correct answers raise your level, mistakes ease it back. The difficulty changes live during a round, and your level is saved between rounds.
- **Always in-your-head.** Ten levels, from `4 + 7` up to things like `13 × 14`, `25% of 84` or `46 × 11`. Nothing needs pen and paper.
- **Streaks and combos.** Every 5 in a row raises your multiplier (up to ×4). Faster answers earn bonus points.

## Run it in Android Studio

1. In Android Studio choose **File → New → Project from Version Control…** (or **Get from VCS** on the welcome screen).
2. Paste `https://github.com/nama12312/mentalmath` and click **Clone**.
3. Wait for the Gradle sync to finish (the first one downloads tools and can take a few minutes).
4. Pick an emulator or plug in your phone, then press the green **Run ▶** button.

## Project layout

```
app/src/main/java/com/mentalmath/game/
├── MainActivity.java      Home screen (level, best score, Play)
├── GameActivity.java      The 60-second round, keypad and animations
├── ResultActivity.java    End-of-round stats
├── PlayerStore.java       Saves level and best score on the phone
└── logic/                 Plain Java, no Android code, unit tested
    ├── QuestionGenerator.java   What each level asks
    ├── SkillTracker.java        How the level goes up and down
    └── ScoreKeeper.java         Points, streaks, multiplier
```

To make a level harder or easier, edit `QuestionGenerator.make()`. To change how quickly levels move, edit `SkillTracker.record()`.
