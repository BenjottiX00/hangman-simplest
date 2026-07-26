package fr.quentincillierre.hangman.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HangmanModelTest {

    @Test
    void testGameInitialization() {
        HangmanModel model = new HangmanModel(Difficulty.EASY);
        assertNotNull(model.getFullWord());
        assertNotNull(model.getCategory());
        assertEquals(0, model.getMistakes());
        assertFalse(model.isGameOver());
        assertFalse(model.isVictory());
    }

    @Test
    void testCorrectGuess() {
        HangmanModel model = new HangmanModel(Difficulty.EASY);
        String word = model.getFullWord();
        // Get the first character of the generated word
        char firstChar = word.charAt(0);
        
        boolean result = model.guessLetter(firstChar);
        assertTrue(result, "Guessing a correct letter should return true");
        assertEquals(0, model.getMistakes(), "Mistakes should not increase on a correct guess");
    }

    @Test
    void testIncorrectGuess() {
        HangmanModel model = new HangmanModel(Difficulty.EASY);
        // Assuming '!' is never in our valid word list
        boolean result = model.guessLetter('!');
        
        assertFalse(result, "Guessing an incorrect letter should return false");
        assertEquals(1, model.getMistakes(), "Mistakes should increase by 1 on an incorrect guess");
    }

    @Test
    void testDuplicateGuess() {
        HangmanModel model = new HangmanModel(Difficulty.EASY);
        char guess = 'A';
        
        model.guessLetter(guess); // First guess
        int mistakesAfterFirst = model.getMistakes();
        
        boolean result2 = model.guessLetter(guess); // Second guess of the same letter
        assertFalse(result2, "Guessing a duplicate letter should return false");
        assertEquals(mistakesAfterFirst, model.getMistakes(), "Mistakes should not increase on a duplicate guess");
    }

    @Test
    void testRevealHintRevealsAnUnrevealedLetter() {
        HangmanModel model = new HangmanModel("APPLE", "FRUITS");

        assertTrue(model.hasHiddenLetters(), "The word should have hidden letters to reveal");
        assertTrue(model.revealHint(), "A hint should be available when letters are still hidden");
        assertTrue(model.getDisplayWord().contains("A"), "The revealed hint should be shown in the display");
    }

    @Test
    void testRevealHintReturnsFalseWhenNoHiddenLettersRemain() {
        HangmanModel model = new HangmanModel("A", "FRUITS");

        model.guessLetter('A');
        assertFalse(model.hasHiddenLetters(), "The word should have no hidden letters left");
        assertFalse(model.revealHint(), "No hint should be available once the word is fully revealed");
    }
}