package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.WordRepository;
import fr.quentincillierre.hangman.model.WordRepository.WordAndCategory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class GameController {

    private static final int KEYBOARD_COLUMNS = 6;
    private static final String HANGMAN_VIDEO = "/videos/hangman-progress.mp4";

    @FXML
    private Label wordLabel;

    @FXML
    private Label resultLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private MediaView hangmanMediaView;

    @FXML
    private GridPane keyboardGrid;

    @FXML
    private StackPane gameOverPane;

    private HangmanModel model;
    private WordRepository wordRepository;
    private MediaPlayer hangmanPlayer;

    @FXML
    public void initialize() {
        wordRepository = new WordRepository();
        setupHangmanMedia();
        startNewGame();
    }

    /**
     * Loads the hangman video once. Each wrong guess seeks further into the
     * clip and pauses (rather than swapping between pictures/N-hangman.png
     * images like before), so the player sees the danger scene creep closer
     * to its worst moment as mistakes pile up.
     */
    private void setupHangmanMedia() {
        String url = getClass().getResource(HANGMAN_VIDEO).toExternalForm();
        Media media = new Media(url);

        hangmanPlayer = new MediaPlayer(media);
        hangmanPlayer.setMute(true);
        hangmanPlayer.setAutoPlay(false);
        hangmanMediaView.setMediaPlayer(hangmanPlayer);

        // Duration isn't known until the media finishes loading; once it is,
        // make sure we're showing the frame that matches the current game state.
        hangmanPlayer.setOnReady(() -> Platform.runLater(this::seekHangmanFrame));
    }

    private void seekHangmanFrame() {
        if (hangmanPlayer == null || model == null || hangmanPlayer.getMedia() == null) {
            return;
        }

        Duration total = hangmanPlayer.getMedia().getDuration();
        if (total == null || total.isUnknown() || total.isIndefinite()) {
            return;
        }

        double fraction = Math.min(1.0, model.getCurrentWrongs() / (double) model.getMaxWrongs());
        hangmanPlayer.pause();
        hangmanPlayer.seek(total.multiply(fraction));
    }

    private void startNewGame() {
        WordAndCategory selection = wordRepository.getRandomWord();
        this.model = new HangmanModel(selection.word(), selection.category());

        categoryLabel.setText("Category: " + model.getCategory());
        resultLabel.setOpacity(0);
        keyboardGrid.setDisable(false);
        keyboardGrid.getChildren().clear();

        // Hide the restart popup until the game actually ends
        gameOverPane.setVisible(false);
        gameOverPane.setManaged(false);

        generateKeyboard();
        refreshUI();
    }

    @FXML
    private void handleRestart() {
        startNewGame();
    }

    private void refreshUI() {
        wordLabel.setText(model.getHiddenWord());

        seekHangmanFrame();

        if (model.isLose() || model.isWin()) {
            keyboardGrid.setDisable(true);

            String finalWord = String.join(" ", model.getWordToGuess().split(""));
            wordLabel.setText(finalWord);

            resultLabel.setOpacity(1);
            resultLabel.setAlignment(Pos.CENTER);

            if (model.isWin()) {
                resultLabel.setText("Victory");
            } else {
                resultLabel.setText("Game Over");
            }

            // Reveal the restart popup now that the game has ended
            gameOverPane.setVisible(true);
            gameOverPane.setManaged(true);
        }
    }

    private void generateKeyboard() {
        for (char c = 'A'; c <= 'Z'; c++) {
            Button letterButton = new Button(String.valueOf(c));
            letterButton.setMaxWidth(Double.MAX_VALUE);
            letterButton.setMaxHeight(Double.MAX_VALUE);

            char letter = c;
            letterButton.setOnAction(event -> {
                letterButton.setDisable(true);
                model.tryLetter(letter);
                refreshUI();
            });

            int index = (int) c - 'A';
            int col = index % KEYBOARD_COLUMNS;
            int row = index / KEYBOARD_COLUMNS;

            keyboardGrid.add(letterButton, col, row);
        }
    }
}