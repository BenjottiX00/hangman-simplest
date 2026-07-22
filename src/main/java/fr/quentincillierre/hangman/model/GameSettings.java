package fr.quentincillierre.hangman.model;

public class GameSettings {
    // Defaults to EASY if the user doesn't pick one
    private static Difficulty currentDifficulty = Difficulty.EASY;

    public static Difficulty getDifficulty() {
        return currentDifficulty;
    }

    public static void setDifficulty(Difficulty difficulty) {
        currentDifficulty = difficulty;
    }
}