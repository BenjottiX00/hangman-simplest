package fr.quentincillierre.hangman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSettingsTest {

    @Test
    void hintCostsFollowDifficulty() {
        assertEquals(2, GameSettings.getHintCost(Difficulty.EASY));
        assertEquals(3, GameSettings.getHintCost(Difficulty.MEDIUM));
        assertEquals(4, GameSettings.getHintCost(Difficulty.HARD));
    }
}
