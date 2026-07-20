package fr.quentincillierre.hangman.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class HangmanModel {
    private final String wordToGuess;
    private final String category;
    private final int maxWrongs;
    private int currentWrongs;
    private Set<Character> guessedLetter;

    public HangmanModel(String wordToGuess, String category) {
        this.wordToGuess = wordToGuess.toUpperCase();
        this.category = category;
        this.maxWrongs = 10;
        this.currentWrongs = 0;
        this.guessedLetter = new LinkedHashSet<>();
    }

    public String getCategory() {
        return category;
    }

    public Set<Character> getGuessedLetter() {
        return guessedLetter;
    }

    public int getCurrentWrongs() {
        return currentWrongs;
    }

    public int getMaxWrongs() {
        return maxWrongs;
    }

    public String getHint() {
        return category;
    }

    public String getWordToGuess(){
        return this.wordToGuess;
    }

    public void tryLetter(Character letter){
        letter = Character.toUpperCase(letter);
        if (this.guessedLetter.contains(letter)){
            return;
        }
        if (!wordToGuess.contains(letter.toString())){
            currentWrongs++;
        }
        guessedLetter.add(letter);
    }

    public String getHiddenWord(){
        StringBuilder hiddenWord = new StringBuilder();

        for (int i = 0; i < wordToGuess.length(); i++){
            Character letter = wordToGuess.charAt(i);
            if (guessedLetter.contains(letter)){
                hiddenWord.append(letter);
            } else {
                hiddenWord.append('_');
            }
            hiddenWord.append(" "); 
        }
        return hiddenWord.toString().trim();
    }

    public boolean isWin(){
        for (Character letter : this.wordToGuess.toCharArray()){
            if (!this.guessedLetter.contains(letter))
                return false;
        }
        return true;
    }

    public boolean isLose(){
        return currentWrongs >= maxWrongs;
    }
}