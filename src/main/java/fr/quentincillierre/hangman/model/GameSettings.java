package fr.quentincillierre.hangman.model;

import java.util.EnumMap;
import java.util.Map;

public class GameSettings {
    // Defaults to EASY if the user doesn't pick one
    private static Difficulty currentDifficulty = Difficulty.EASY;
    private static final Map<Difficulty, Integer> fishBanks = new EnumMap<>(Difficulty.class);

    static {
        fishBanks.put(Difficulty.EASY, 0);
        fishBanks.put(Difficulty.MEDIUM, 0);
        fishBanks.put(Difficulty.HARD, 0);
    }

    public static Difficulty getDifficulty() {
        return currentDifficulty;
    }

    public static void setDifficulty(Difficulty difficulty) {
        currentDifficulty = difficulty;
    }

    public static int getFishBalance(Difficulty difficulty) {
        return fishBanks.getOrDefault(difficulty, 0);
    }

    public static int getHintCost(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 2;
            case MEDIUM -> 3;
            case HARD -> 4;
        };
    }

    public static void addFish(Difficulty difficulty, int amount) {
        fishBanks.put(difficulty, Math.max(0, getFishBalance(difficulty) + amount));
    }

    public static void spendFish(Difficulty difficulty, int amount) {
        fishBanks.put(difficulty, Math.max(0, getFishBalance(difficulty) - amount));
    }
}