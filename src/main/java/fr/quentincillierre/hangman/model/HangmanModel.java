package fr.quentincillierre.hangman.model;

import java.util.HashSet;
import java.util.Set;

public class HangmanModel {
    private WordRepository.Word currentWord;
    private Set<Character> guessedLetters;
    private int mistakes;
    private static final int MAX_MISTAKES = 10;

    public HangmanModel(Difficulty difficulty) {
        this(WordRepository.getRandomWord(difficulty));
    }

    public HangmanModel(String word, String category) {
        this(new WordRepository.Word(word, category));
    }

    private HangmanModel(WordRepository.Word word) {
        this.currentWord = word;
        this.guessedLetters = new HashSet<>();
        this.mistakes = 0;

        // Auto-reveal spaces so multi-word phrases don't block victories in Hard Mode
        this.guessedLetters.add(' ');
    }

    public boolean guessLetter(char letter) {
        if (isGameOver() || isVictory()) return false;

        char upperLetter = Character.toUpperCase(letter);
        if (guessedLetters.contains(upperLetter)) {
            return false;
        }

        guessedLetters.add(upperLetter);

        if (!currentWord.text.contains(String.valueOf(upperLetter))) {
            mistakes++;
            return false;
        }
        return true;
    }

    public boolean revealHint() {
        if (isGameOver() || isVictory()) return false;

        for (char c : currentWord.text.toCharArray()) {
            char upperChar = Character.toUpperCase(c);
            if (upperChar != ' ' && !guessedLetters.contains(upperChar)) {
                guessedLetters.add(upperChar);
                return true;
            }
        }
        return false;
    }

    public boolean hasHiddenLetters() {
        for (char c : currentWord.text.toCharArray()) {
            char upperChar = Character.toUpperCase(c);
            if (upperChar != ' ' && !guessedLetters.contains(upperChar)) {
                return true;
            }
        }
        return false;
    }

    public String getDisplayWord() {
        StringBuilder display = new StringBuilder();
        for (char c : currentWord.text.toCharArray()) {
            if (guessedLetters.contains(c)) {
                display.append(c).append(" ");
            } else {
                display.append("_ ");
            }
        }
        return display.toString().trim();
    }

    public String getCategory() {
        return currentWord.category;
    }

    public String getFullWord() {
        return currentWord.text;
    }

    public int getMistakes() {
        return mistakes;
    }

    public boolean isGameOver() {
        return mistakes >= MAX_MISTAKES;
    }

    public boolean isVictory() {
        for (char c : currentWord.text.toCharArray()) {
            if (!guessedLetters.contains(c)) {
                return false;
            }
        }
        return true;
    }
}