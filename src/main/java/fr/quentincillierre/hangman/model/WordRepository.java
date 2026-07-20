package fr.quentincillierre.hangman.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordRepository {

    // Container for a word and the category it belongs to
    public record WordAndCategory(String word, String category) {}

    /*
     * To add a brand-new category:
     *   1. Drop a new .txt file into src/main/resources/categories/
     *      (first line = category title shown to the player, one word per line after that)
     *   2. Add its file name to this list.
     * No other Java changes are needed - the word lists themselves can be edited
     * freely (add/remove/reword) without touching any code at all.
     */
    private static final String[] CATEGORY_FILES = {
            "sports.txt",
            "animals.txt",
            "fruits.txt",
            "countries.txt",
            "colors.txt",
            "movies.txt",
            "music.txt",
            "professions.txt",
            "vehicles.txt",
            "weather.txt"
    };

    private static final String CATEGORY_FOLDER = "/categories/";

    private final Map<String, List<String>> categories;
    private final Random random;

    public WordRepository() {
        this.categories = new LinkedHashMap<>();
        this.random = new Random();
        loadCategories();
    }

    private void loadCategories() {
        for (String fileName : CATEGORY_FILES) {
            String resourcePath = CATEGORY_FOLDER + fileName;
            try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
                if (in == null) {
                    System.err.println("Category file not found on classpath: " + resourcePath);
                    continue;
                }
                loadCategoryFile(in, fileName);
            } catch (IOException e) {
                System.err.println("Failed to read category file: " + resourcePath + " (" + e.getMessage() + ")");
            }
        }

        if (categories.isEmpty()) {
            throw new IllegalStateException(
                    "No word categories could be loaded. Make sure the .txt files listed in "
                    + "CATEGORY_FILES exist under src/main/resources" + CATEGORY_FOLDER);
        }
    }

    private void loadCategoryFile(InputStream in, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String title = null;
            List<String> words = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue; // skip blank lines and comment lines
                }
                if (title == null) {
                    title = trimmed.toUpperCase();
                } else {
                    words.add(trimmed.toUpperCase());
                }
            }

            if (title == null || words.isEmpty()) {
                System.err.println("Category file " + fileName + " has no title or no words - skipping.");
                return;
            }

            categories.put(title, words);
        }
    }

    public WordAndCategory getRandomWord() {
        List<String> categoryNames = new ArrayList<>(categories.keySet());
        String category = categoryNames.get(random.nextInt(categoryNames.size()));
        List<String> words = categories.get(category);
        String word = words.get(random.nextInt(words.size()));
        return new WordAndCategory(word, category);
    }
}
