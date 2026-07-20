package fr.quentincillierre.hangman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HangmanModelTest {

    private HangmanModel model;

    @BeforeEach
    void setUp() {
        // Pass a word and a hint
        model = new HangmanModel("java", "A programming language");
    }

    @Test
    void testConstructor() {
        assertEquals("JAVA", model.getWordToGuess().toUpperCase());
        assertEquals(0, model.getCurrentWrongs());
        assertTrue(model.getGuessedLetter().isEmpty());
        assertEquals("A programming language", model.getHint());
    }

    @Test
    void testTryLetterWithCorrectLetter() {
        model.tryLetter('j');
        assertEquals(0, model.getCurrentWrongs());
        assertTrue(model.getGuessedLetter().contains('J'));
        assertEquals("J _ _ _", model.getHiddenWord().toUpperCase());
    }

    @Test
    void testTryLetterWithIncorrectLetter() {
        model.tryLetter('x');
        assertEquals(1, model.getCurrentWrongs());
        assertTrue(model.getGuessedLetter().contains('X'));
        assertEquals("_ _ _ _", model.getHiddenWord());
    }

    @Test
    void testTryLetterWithDuplicate() {
        model.tryLetter('j');
        model.tryLetter('j');
        assertEquals(1, model.getGuessedLetter().size());
        assertEquals(0, model.getCurrentWrongs());
    }

    @Test
    void testTryLetterCaseInsensitive() {
        model.tryLetter('J');
        model.tryLetter('a');
        assertEquals("J A _ A", model.getHiddenWord().toUpperCase());
        assertEquals(0, model.getCurrentWrongs());
    }

    @Test
    void testGetHiddenWord() {
        assertEquals("_ _ _ _", model.getHiddenWord());
        model.tryLetter('j');
        assertEquals("J _ _ _", model.getHiddenWord().toUpperCase());
        model.tryLetter('a');
        assertEquals("J A _ A", model.getHiddenWord().toUpperCase());
        model.tryLetter('v');
        assertEquals("J A V A", model.getHiddenWord().toUpperCase());
    }

    @Test
    void testIsWin() {
        assertFalse(model.isWin());
        model.tryLetter('j');
        model.tryLetter('a');
        model.tryLetter('v');
        assertTrue(model.isWin());
    }

    @Test
    void testIsLose() {
        assertFalse(model.isLose());
        model.tryLetter('z');
        model.tryLetter('q');
        model.tryLetter('p');
        model.tryLetter('o');
        model.tryLetter('i');
        model.tryLetter('u');
        model.tryLetter('y');
        model.tryLetter('t');
        model.tryLetter('r');
        model.tryLetter('e');
        assertTrue(model.isLose());
    }

    @Test
    void testIsWinWithPartialGuess() {
        model.tryLetter('j');
        model.tryLetter('a');
        assertFalse(model.isWin());
    }

    @Test
    void testGetGuessedLetter() {
        model.tryLetter('j');
        model.tryLetter('a');
        Set<Character> guessed = model.getGuessedLetter();
        assertEquals(2, guessed.size());
        assertTrue(guessed.contains('J'));
        assertTrue(guessed.contains('A'));
    }

    @Test
    void testMaxWrongGuesses() {
        model.tryLetter('z');
        model.tryLetter('q');
        model.tryLetter('p');
        model.tryLetter('o');
        model.tryLetter('i');
        model.tryLetter('u');
        model.tryLetter('y');
        model.tryLetter('t');
        model.tryLetter('r');
        assertFalse(model.isLose());
        model.tryLetter('x');
        assertTrue(model.isLose());
    }
}