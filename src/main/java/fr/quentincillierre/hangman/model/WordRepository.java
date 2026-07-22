package fr.quentincillierre.hangman.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordRepository {
    private static final String[] CATEGORIES = {
        "animals.txt", "colors.txt", "countries.txt", "fruits.txt",
        "movies.txt", "music.txt", "professions.txt", "sports.txt",
        "vehicles.txt", "weather.txt"
    };

    public static Word getRandomWord(Difficulty difficulty) {
        Random random = new Random();
        String categoryFile = CATEGORIES[random.nextInt(CATEGORIES.length)];
        String categoryName = categoryFile.replace(".txt", "").toUpperCase();
        
        List<String> validWords = new ArrayList<>();
        
        try (InputStream is = WordRepository.class.getResourceAsStream("/" + categoryFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { 
                    isFirstLine = false; // Skip the category header line
                    continue;
                }
                String word = line.trim().toUpperCase();
                if (word.isEmpty()) continue;

                boolean isValid = false;
                switch (difficulty) {
                    case EASY:
                        isValid = word.length() <= 7 && !word.contains(" ");
                        break;
                    case MEDIUM:
                        isValid = word.length() >= 7 && !word.contains(" ");
                        break;
                    case HARD:
                        isValid = word.length() >= 10 || word.contains(" ");
                        break;
                }
                
                if (isValid) {
                    validWords.add(word);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Word("ERROR", "DEFAULT");
        }

        // Fallback in case a category doesn't have enough words for a specific difficulty
        if (validWords.isEmpty()) {
            return new Word("HANGMAN", categoryName);
        }

        String selectedWord = validWords.get(random.nextInt(validWords.size()));
        return new Word(selectedWord, categoryName);
    }

    public static class Word {
        public final String text;
        public final String category;
        public Word(String text, String category) {
            this.text = text;
            this.category = category;
        }
    }
}