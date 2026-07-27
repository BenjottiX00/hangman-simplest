package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.GameSettings;
import fr.quentincillierre.hangman.model.HangmanModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import java.io.InputStream;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;


import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML private AnchorPane gameBoard;
    @FXML private StackPane overlayPane;
    @FXML private Label categoryLabel;
    @FXML private Label wordLabel;
    @FXML private Label resultLabel;
    @FXML private TilePane keyboardPane;
    @FXML private MediaView hangmanMediaView;
    @FXML private MediaView tryAgainMediaView;
    @FXML private ImageView lineOverlay;
    @FXML private Button homeButton;
    @FXML private Button fishButton;

    private HangmanModel model;
    private MediaPlayer mistakePlayer;
    private MediaPlayer tryAgainPlayer;
    private MediaPlayer gameSoundPlayer;
    private MediaPlayer victorySoundPlayer;
    private MediaPlayer defeatSoundPlayer;
    private javafx.scene.media.Media wrongSoundMedia;
    private javafx.scene.media.Media rightSoundMedia;
    private javafx.scene.media.Media buttonSoundMedia;
    private javafx.scene.media.Media victorySoundMedia;
    private javafx.scene.media.Media defeatSoundMedia;
    private int currentMistakes = -1;
    private javafx.animation.ParallelTransition shakeTransition;
    private static GameController activeInstance;
    private int currentRoundFish = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeInstance = this;
        Font.loadFont(getClass().getResourceAsStream("/fonts/PermanentMarker-Regular.ttf"), 14);
        // load decorative overlay image programmatically to ensure classpath resolution
        try (InputStream is = getClass().getResourceAsStream("/videos/line.png")) {
            if (is != null && lineOverlay != null) {
                lineOverlay.setImage(new Image(is));
            } else if (is == null) {
                System.err.println("Overlay image /videos/line.png not found on classpath");
            }
        } catch (Exception e) {
            System.err.println("Failed to load overlay image: " + e.getMessage());
        }

        // initial overlay tuning values (adjust these if you want nudging)
        final double overlayOffsetX = -124; // negative moves left, positive moves right
        final double overlayOffsetY = -140;  // negative moves up, positive moves down
        final double overlayScale = 0.62;   // bigger overlay (55% of media fit)

        // apply responsive sizing and positioning once layout is ready
        Platform.runLater(() -> {
            if (lineOverlay != null && hangmanMediaView != null) {
                lineOverlay.setPreserveRatio(true);
                lineOverlay.setFitWidth(hangmanMediaView.getFitWidth() * overlayScale);
                lineOverlay.setFitHeight(hangmanMediaView.getFitHeight() * overlayScale);
                lineOverlay.setTranslateX(overlayOffsetX);
                lineOverlay.setTranslateY(overlayOffsetY);

                hangmanMediaView.fitWidthProperty().addListener((obs, oldV, newV) -> {
                    lineOverlay.setFitWidth(newV.doubleValue() * overlayScale);
                });
                hangmanMediaView.fitHeightProperty().addListener((obs, oldV, newV) -> {
                    lineOverlay.setFitHeight(newV.doubleValue() * overlayScale);
                });
            }
        });

        setupFishButton();
        startNewGame();
    }

    private void startNewGame() {
        model = new HangmanModel(GameSettings.getDifficulty());
        currentRoundFish = 10;
        currentMistakes = -1;
        categoryLabel.setText(model.getCategory());
        updateWordDisplay();
        setupKeyboard();
        updateFishButton();
        updateMistakeVideo();

        // Start looping game stage sound (only during gameplay)
        try {
            // Stop any existing game sound first
            if (gameSoundPlayer != null) {
                try { gameSoundPlayer.stop(); gameSoundPlayer.dispose(); } catch (Exception ignored) {}
                gameSoundPlayer = null;
            }
            MediaLoader.createMediaPlayerWithRetries("soundEffects/GameSound.mp3", 2, 500, sp -> {
                try {
                    gameSoundPlayer = sp;
                    gameSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    gameSoundPlayer.setVolume(0.35);
                    gameSoundPlayer.play();
                } catch (Exception ignored) {}
            }, err -> System.err.println("Could not load game sound: " + err));
        } catch (Exception e) {
            System.err.println("Could not start game sound: " + e.getMessage());
        }

        // Prepare wrong-press sound for mistakes
        try {
            String wrongPath = "soundEffects/Wrong.mp3";
            if (MediaLoader.exists(wrongPath)) {
                try { MediaLoader.preload(wrongPath); } catch (Exception ignored) {}
                try { wrongSoundMedia = MediaLoader.load(wrongPath); } catch (Exception ex) { wrongSoundMedia = null; }
            }
        } catch (Exception ex) {
            System.err.println("Could not preload wrong sound: " + ex.getMessage());
            wrongSoundMedia = null;
        }

        // Prepare right-press sound for correct guesses
        try {
            String rightPath = "soundEffects/Right.mp3";
            if (MediaLoader.exists(rightPath)) {
                try { MediaLoader.preload(rightPath); } catch (Exception ignored) {}
                try { rightSoundMedia = MediaLoader.load(rightPath); } catch (Exception ex) { rightSoundMedia = null; }
            }
        } catch (Exception ex) {
            System.err.println("Could not preload right sound: " + ex.getMessage());
            rightSoundMedia = null;
        }

        // Prepare button-press sound for try-again panel and menu buttons
        try {
            String preferred = "soundEffects/menuButton.mp3";
            String fallback = "soundEffects/menuButtons_soundtrack.mp3";
            String chosen = MediaLoader.exists(preferred) ? preferred : (MediaLoader.exists(fallback) ? fallback : null);
            if (chosen != null) {
                try { MediaLoader.preload(chosen); } catch (Exception ignored) {}
                try { buttonSoundMedia = MediaLoader.load(chosen); } catch (Exception ex) { buttonSoundMedia = null; }
            }
        } catch (Exception ex) {
            System.err.println("Could not preload button sound: " + ex.getMessage());
            buttonSoundMedia = null;
        }

        // Prepare victory and defeat sounds for the try-again panel
        try {
            String victoryPath = "soundEffects/victory.mp3";
            if (MediaLoader.exists(victoryPath)) {
                try { MediaLoader.preload(victoryPath); } catch (Exception ignored) {}
                try { victorySoundMedia = MediaLoader.load(victoryPath); } catch (Exception ex) { victorySoundMedia = null; }
            }
        } catch (Exception ex) {
            System.err.println("Could not preload victory sound: " + ex.getMessage());
            victorySoundMedia = null;
        }

        try {
            String defeatPath = "soundEffects/Defeat.mp3";
            if (MediaLoader.exists(defeatPath)) {
                try { MediaLoader.preload(defeatPath); } catch (Exception ignored) {}
                try { defeatSoundMedia = MediaLoader.load(defeatPath); } catch (Exception ex) { defeatSoundMedia = null; }
            }
        } catch (Exception ex) {
            System.err.println("Could not preload defeat sound: " + ex.getMessage());
            defeatSoundMedia = null;
        }
    }

    private void setupFishButton() {
        if (fishButton == null) {
            return;
        }

        fishButton.setContentDisplay(ContentDisplay.CENTER);
        fishButton.setGraphicTextGap(0);
    }

    private void updateFishButton() {
        if (fishButton == null) {
            return;
        }
        Difficulty difficulty = GameSettings.getDifficulty();
        int balance = GameSettings.getFishBalance(difficulty);
        int hintCost = GameSettings.getHintCost(difficulty);
        fishButton.setText(String.format("🐟 %d  BUY HINT", balance));
        fishButton.setDisable(balance < hintCost || model == null || !model.hasHiddenLetters());
    }

    private void updateMistakeVideo() {
        int mistakes = model.getMistakes();
        if (mistakes == currentMistakes) {
            return;
        }

        final int expectedMistakeStage = mistakes;
        currentMistakes = mistakes;
        String fileBase = String.format("videos/catvideo (%d)", mistakes);
        String partA = fileBase + " A.mp4";
        String partB = fileBase + " B.mp4";
        String singleFile = fileBase + ".mp4";

        try {
            if (mistakes == 10) {
                fadeOutGameSound();
                disableGameInput();
                playFinalCatVideo(partA, partB, singleFile);
                return;
            }

            if (MediaLoader.exists(partA) && MediaLoader.exists(partB)) {
                playPartAThenLoopPartB(partA, partB);
                return;
            }

            String videoFile = MediaLoader.exists(singleFile) ? singleFile : partA;

            // if the media failed validation during preload, avoid creating a MediaPlayer for it
            if (!MediaLoader.isPreloaded(videoFile)) {
                System.err.println("Skipping mistake video (not preloaded/invalid): " + videoFile);
                return;
            }

            final MediaPlayer oldPlayer = mistakePlayer;
            MediaLoader.createMediaPlayerWithRetries(videoFile, 2, 700, mp -> {
                if (!isCurrentMistakeStage(expectedMistakeStage)) {
                    try { mp.stop(); mp.dispose(); } catch (Exception ignored) {}
                    return;
                }
                try {
                    mp.setCycleCount(MediaPlayer.INDEFINITE);
                    hangmanMediaView.setMediaPlayer(mp);
                    hangmanMediaView.setOpacity(1.0);
                    mp.play();
                    if (oldPlayer != null) {
                        try { oldPlayer.stop(); oldPlayer.dispose(); } catch (Exception ignored) {}
                    }
                    mistakePlayer = mp;
                } catch (Exception e) {
                    System.err.println("Error while switching to new media " + videoFile + ": " + e.getMessage());
                }
            }, err -> {
                if (!isCurrentMistakeStage(expectedMistakeStage)) {
                    return;
                }
                System.err.println("MediaPlayer error loading " + videoFile + ": " + err);
                if (oldPlayer != null) {
                    try { hangmanMediaView.setMediaPlayer(oldPlayer); oldPlayer.play(); } catch (Exception ignored) {}
                }
            });

        } catch (Exception e) {
            System.err.println("Could not load mistake video for " + fileBase + ": " + e.getMessage());
        }
    }


    private void playPartAThenLoopPartB(String partA, String partB) {
        final int expectedMistakeStage = model.getMistakes();
        try {
            final MediaPlayer oldPlayer = mistakePlayer;
            if (!MediaLoader.isPreloaded(partA)) {
                System.err.println("Skipping partA (not preloaded/invalid): " + partA);
                return;
            }

            MediaLoader.createMediaPlayerWithRetries(partA, 2, 700, partAPlayer -> {
                if (!isCurrentMistakeStage(expectedMistakeStage)) {
                    try { partAPlayer.stop(); partAPlayer.dispose(); } catch (Exception ignored) {}
                    return;
                }
                partAPlayer.setCycleCount(1);
                hangmanMediaView.setMediaPlayer(partAPlayer);
                partAPlayer.play();
                if (oldPlayer != null) {
                    try { oldPlayer.stop(); oldPlayer.dispose(); } catch (Exception ignored) {}
                }
                mistakePlayer = partAPlayer;

                partAPlayer.setOnEndOfMedia(() -> {
                    try {
                        partAPlayer.dispose();
                    } catch (Exception ignored) {
                    }

                    try {
                        if (!isCurrentMistakeStage(expectedMistakeStage)) {
                            return;
                        }
                        if (!MediaLoader.isPreloaded(partB)) {
                            System.err.println("Skipping partB loop (not preloaded/invalid): " + partB);
                            return;
                        }
                        MediaLoader.createMediaPlayerWithRetries(partB, 2, 700, partBPlayer -> {
                            if (!isCurrentMistakeStage(expectedMistakeStage)) {
                                try { partBPlayer.stop(); partBPlayer.dispose(); } catch (Exception ignored) {}
                                return;
                            }
                            partBPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                            hangmanMediaView.setMediaPlayer(partBPlayer);
                            partBPlayer.play();
                            mistakePlayer = partBPlayer;
                        }, err -> {
                            if (!isCurrentMistakeStage(expectedMistakeStage)) {
                                return;
                            }
                            System.err.println("Error loading partB " + partB + ": " + err);
                        });
                    } catch (Exception ex) {
                        System.err.println("Could not load loop video " + partB + ": " + ex.getMessage());
                    }
                });
            }, err -> {
                if (!isCurrentMistakeStage(expectedMistakeStage)) {
                    return;
                }
                System.err.println("Error loading partA " + partA + ": " + err);
            });

        } catch (Exception e) {
            System.err.println("Could not load part-A mistake video " + partA + ": " + e.getMessage());
        }
    }

    private void playFinalCatVideo(String partA, String partB, String singleFile) {
        final int expectedMistakeStage = model.getMistakes();
        try {
            if (MediaLoader.exists(partA) && MediaLoader.exists(partB)) {
                final MediaPlayer oldPlayer = mistakePlayer;
                if (!MediaLoader.isPreloaded(partA)) {
                    System.err.println("Skipping final partA (not preloaded/invalid): " + partA);
                    // fallback to single-file if available and preloaded
                    if (MediaLoader.isPreloaded(singleFile)) {
                        playSingleFinal(singleFile, oldPlayer);
                        return;
                    }
                    showFinalGameOver();
                    return;
                }

                MediaLoader.createMediaPlayerWithRetries(partA, 2, 700, partAPlayer -> {
                    if (!isCurrentMistakeStage(expectedMistakeStage)) {
                        try { partAPlayer.stop(); partAPlayer.dispose(); } catch (Exception ignored) {}
                        return;
                    }
                    partAPlayer.setCycleCount(1);
                    hangmanMediaView.setMediaPlayer(partAPlayer);
                    hangmanMediaView.setOpacity(1.0);
                    partAPlayer.play();
                    if (oldPlayer != null) {
                        try { oldPlayer.stop(); oldPlayer.dispose(); } catch (Exception ignored) {}
                    }
                    mistakePlayer = partAPlayer;

                    partAPlayer.setOnEndOfMedia(() -> {
                        try { partAPlayer.dispose(); } catch (Exception ignored) {}
                        try {
                            if (!isCurrentMistakeStage(expectedMistakeStage)) {
                                return;
                            }
                            if (!MediaLoader.isPreloaded(partB)) {
                                System.err.println("Skipping final partB (not preloaded/invalid): " + partB);
                                showFinalGameOver();
                                return;
                            }
                            MediaLoader.createMediaPlayerWithRetries(partB, 2, 700, partBPlayer -> {
                                if (!isCurrentMistakeStage(expectedMistakeStage)) {
                                    try { partBPlayer.stop(); partBPlayer.dispose(); } catch (Exception ignored) {}
                                    return;
                                }
                                partBPlayer.setCycleCount(1);
                                hangmanMediaView.setMediaPlayer(partBPlayer);
                                hangmanMediaView.setOpacity(1.0);
                                partBPlayer.play();
                                mistakePlayer = partBPlayer;
                                final PauseTransition watchdog = new PauseTransition(javafx.util.Duration.seconds(20));
                                watchdog.setOnFinished(evt -> {
                                    System.err.println("Final partB playback timeout, proceeding to end game");
                                    showFinalGameOver();
                                });
                                partBPlayer.setOnEndOfMedia(() -> {
                                    watchdog.stop();
                                    showFinalGameOver();
                                });
                                watchdog.play();
                            }, err -> {
                                if (!isCurrentMistakeStage(expectedMistakeStage)) {
                                    return;
                                }
                                System.err.println("Error loading final partB " + partB + ": " + err);
                                showFinalGameOver();
                            });
                        } catch (Exception ex) {
                            System.err.println("Could not load final video part B " + partB + ": " + ex.getMessage());
                            showFinalGameOver();
                        }
                    });
                }, err -> {
                    if (!isCurrentMistakeStage(expectedMistakeStage)) {
                        return;
                    }
                    System.err.println("Error loading final partA " + partA + ": " + err);
                    showFinalGameOver();
                });
            } else {
                final MediaPlayer oldPlayer = mistakePlayer;
                if (!MediaLoader.isPreloaded(singleFile)) {
                    System.err.println("Skipping final single (not preloaded/invalid): " + singleFile);
                    showFinalGameOver();
                    return;
                }

                MediaLoader.createMediaPlayerWithRetries(singleFile, 2, 700, finalPlayer -> {
                    if (!isCurrentMistakeStage(expectedMistakeStage)) {
                        try { finalPlayer.stop(); finalPlayer.dispose(); } catch (Exception ignored) {}
                        return;
                    }
                    finalPlayer.setCycleCount(1);
                    hangmanMediaView.setMediaPlayer(finalPlayer);
                    hangmanMediaView.setOpacity(1.0);
                    finalPlayer.play();
                    if (oldPlayer != null) {
                        try { oldPlayer.stop(); oldPlayer.dispose(); } catch (Exception ignored) {}
                    }
                    mistakePlayer = finalPlayer;
                    finalPlayer.setOnEndOfMedia(this::showFinalGameOver);
                }, err -> {
                    if (!isCurrentMistakeStage(expectedMistakeStage)) {
                        return;
                    }
                    System.err.println("Error loading final single " + singleFile + ": " + err);
                    showFinalGameOver();
                });
            }
        } catch (Exception e) {
            System.err.println("Could not load final mistake video " + singleFile + ": " + e.getMessage());
            showFinalGameOver();
        }
    }

    /**
     * Play a single-file final video with error handling and a watchdog timeout.
     */
    private void playSingleFinal(String singleFile, MediaPlayer oldPlayer) {
        try {
            final MediaPlayer finalPlayer = new MediaPlayer(MediaLoader.load(singleFile));
            finalPlayer.setCycleCount(1);
            finalPlayer.setOnError(() -> {
                System.err.println("Error loading final single " + singleFile + ": " + finalPlayer.getError());
                showFinalGameOver();
            });
            finalPlayer.setOnReady(() -> Platform.runLater(() -> {
                hangmanMediaView.setMediaPlayer(finalPlayer);
                hangmanMediaView.setOpacity(1.0);
                finalPlayer.play();
                if (oldPlayer != null) {
                    try { oldPlayer.stop(); oldPlayer.dispose(); } catch (Exception ignored) {}
                }
                mistakePlayer = finalPlayer;
                final PauseTransition watchdog = new PauseTransition(javafx.util.Duration.seconds(20));
                watchdog.setOnFinished(evt -> {
                    System.err.println("Final single playback timeout, proceeding to end game");
                    showFinalGameOver();
                });
                finalPlayer.setOnEndOfMedia(() -> {
                    watchdog.stop();
                    showFinalGameOver();
                });
                watchdog.play();
            }));
        } catch (Exception ex) {
            System.err.println("Could not load final single " + singleFile + ": " + ex.getMessage());
            showFinalGameOver();
        }
    }

    private void showFinalGameOver() {
        Platform.runLater(() -> showEndGame(false));
    }

    private void playTryAgainVideo(boolean isVictory) {
        try {
            stopTryAgainAudio();
            String videoPath = isVictory ? "videos/victoryTryAgain.mp4" : "videos/defeatTryAgain.mp4";
            if (!MediaLoader.exists(videoPath) || !MediaLoader.isPreloaded(videoPath)) {
                System.err.println("Try again video not available or invalid: " + videoPath);
                return;
            }

            if (tryAgainPlayer != null) {
                tryAgainPlayer.stop();
                tryAgainPlayer.dispose();
            }

            MediaLoader.createMediaPlayerWithRetries(videoPath, 2, 700, mp -> {
                tryAgainPlayer = mp;
                tryAgainPlayer.setCycleCount(javafx.scene.media.MediaPlayer.INDEFINITE);
                // increase try-again video volume
                tryAgainPlayer.setVolume(1.0);
                tryAgainMediaView.setMediaPlayer(tryAgainPlayer);
                tryAgainPlayer.play();
            }, err -> System.err.println("Could not play try again video: " + err));

            if (isVictory) {
                playVictorySound();
            } else {
                playDefeatSound();
            }
        } catch (Exception e) {
            System.err.println("Failed to load try again video: " + e.getMessage());
        }
    }

    private void fadeInFullWord() {
        fadeInFullWord(null);
    }

    private void fadeInFullWord(Runnable onFinished) {
        if (wordLabel == null || model == null) {
            if (onFinished != null) Platform.runLater(onFinished);
            return;
        }
        Platform.runLater(() -> {
            wordLabel.setText(model.getFullWord());
            wordLabel.setOpacity(0.0);
            FadeTransition fade = new FadeTransition(javafx.util.Duration.millis(100), wordLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            if (onFinished != null) {
                fade.setOnFinished(e -> onFinished.run());
            }
            fade.play();
        });
    }

    private void shakeOnWrong() {
        try {
            if (hangmanMediaView == null && lineOverlay == null) return;

            // stop any running shake and restore originals
            if (shakeTransition != null && shakeTransition.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                shakeTransition.stop();
            }

            final double origMediaY = (hangmanMediaView != null) ? hangmanMediaView.getTranslateY() : 0.0;
            final double origLineY = (lineOverlay != null) ? lineOverlay.getTranslateY() : 0.0;

            TranslateTransition tMedia = null;
            TranslateTransition tLine = null;

            double delta = 8.0;
            int cycles = 6;
            javafx.util.Duration dur = javafx.util.Duration.millis(40);

            if (hangmanMediaView != null) {
                tMedia = new TranslateTransition(dur, hangmanMediaView);
                tMedia.setFromY(origMediaY);
                tMedia.setToY(origMediaY + delta);
                tMedia.setCycleCount(cycles);
                tMedia.setAutoReverse(true);
            }

            if (lineOverlay != null) {
                tLine = new TranslateTransition(dur, lineOverlay);
                tLine.setFromY(origLineY);
                tLine.setToY(origLineY + delta);
                tLine.setCycleCount(cycles);
                tLine.setAutoReverse(true);
            }

            ParallelTransition pt = new ParallelTransition();
            if (tMedia != null) pt.getChildren().add(tMedia);
            if (tLine != null) pt.getChildren().add(tLine);
            shakeTransition = pt;
            pt.setOnFinished(evt -> Platform.runLater(() -> {
                if (hangmanMediaView != null) hangmanMediaView.setTranslateY(origMediaY);
                if (lineOverlay != null) lineOverlay.setTranslateY(origLineY);
            }));
            pt.play();
        } catch (Exception ignored) {
        }
    }

    private void disableGameInput() {
        gameBoard.setDisable(true);
        if (homeButton != null) {
            homeButton.setDisable(true);
        }
    }

    private void setupKeyboard() {
        keyboardPane.getChildren().clear();
        keyboardPane.setDisable(false);
        for (char c = 'A'; c <= 'Z'; c++) {
            Button keyBtn = new Button(String.valueOf(c));
            keyBtn.getStyleClass().add("keyboard-button");
            keyBtn.setPrefSize(45, 45);
            
            keyBtn.setOnAction(e -> {
                keyBtn.setDisable(true);
                handleGuess(keyBtn.getText().charAt(0));
            });
            keyboardPane.getChildren().add(keyBtn);
        }
    }

    private void playBuyHintSound() {
        try {
            String buyHintPath = "soundEffects/buyHint.mp3";
            if (MediaLoader.exists(buyHintPath)) {
                MediaPlayer p = new MediaPlayer(MediaLoader.load(buyHintPath));
                p.setOnEndOfMedia(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
                p.setOnError(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
                p.play();
            }
        } catch (Exception ignored) {}
    }

    private void playWrongSound() {
        if (wrongSoundMedia == null) return;
        try {
            // Play the wrong sound multiple times concurrently to increase perceived loudness
            MediaPlayer p1 = new MediaPlayer(wrongSoundMedia);
            MediaPlayer p2 = new MediaPlayer(wrongSoundMedia);
            p1.setVolume(1.0);
            p2.setVolume(1.0);
            p1.setOnEndOfMedia(() -> { try { p1.stop(); p1.dispose(); } catch (Exception ignored) {} });
            p2.setOnEndOfMedia(() -> { try { p2.stop(); p2.dispose(); } catch (Exception ignored) {} });
            p1.setOnError(() -> { try { p1.stop(); p1.dispose(); } catch (Exception ignored) {} });
            p2.setOnError(() -> { try { p2.stop(); p2.dispose(); } catch (Exception ignored) {} });
            p1.play();
            p2.play();
        } catch (Exception ignored) {}
    }

    private void playRightSound() {
        if (rightSoundMedia == null) return;
        try {
            MediaPlayer p = new MediaPlayer(rightSoundMedia);
            p.setOnEndOfMedia(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
            p.setOnError(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
            p.play();
        } catch (Exception ignored) {}
    }

    private boolean isCurrentMistakeStage(int expected) {
        return model != null && model.getMistakes() == expected;
    }

    private void playVictorySound() {
        if (victorySoundMedia == null) return;
        stopTryAgainAudio();
        try {
            victorySoundPlayer = new MediaPlayer(victorySoundMedia);
            victorySoundPlayer.setCycleCount(1);
            victorySoundPlayer.setOnEndOfMedia(() -> { try { victorySoundPlayer.stop(); victorySoundPlayer.dispose(); victorySoundPlayer = null; } catch (Exception ignored) {} });
            victorySoundPlayer.setOnError(() -> { try { victorySoundPlayer.stop(); victorySoundPlayer.dispose(); victorySoundPlayer = null; } catch (Exception ignored) {} });
            victorySoundPlayer.play();
        } catch (Exception ignored) {}
    }

    private void playDefeatSound() {
        if (defeatSoundMedia == null) return;
        stopTryAgainAudio();
        try {
            defeatSoundPlayer = new MediaPlayer(defeatSoundMedia);
            defeatSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            defeatSoundPlayer.setOnEndOfMedia(() -> { });
            defeatSoundPlayer.setOnError(() -> { try { defeatSoundPlayer.stop(); defeatSoundPlayer.dispose(); defeatSoundPlayer = null; } catch (Exception ignored) {} });
            defeatSoundPlayer.play();
        } catch (Exception ignored) {}
    }

    private void stopTryAgainAudio() {
        if (victorySoundPlayer != null) {
            try { victorySoundPlayer.stop(); victorySoundPlayer.dispose(); } catch (Exception ignored) {}
            victorySoundPlayer = null;
        }
        if (defeatSoundPlayer != null) {
            try { defeatSoundPlayer.stop(); defeatSoundPlayer.dispose(); } catch (Exception ignored) {}
            defeatSoundPlayer = null;
        }
    }

    private void handleGuess(char letter) {
        int previousMistakes = model.getMistakes();
        model.guessLetter(letter);
        updateWordDisplay();

        if (model.getMistakes() != previousMistakes) {
            if (model.getMistakes() > previousMistakes) {
                currentRoundFish = Math.max(0, currentRoundFish - 1);
                updateFishButton();
                playWrongSound();
                // mark the pressed key as wrong (red-gray)
                Platform.runLater(() -> {
                    for (javafx.scene.Node n : keyboardPane.getChildren()) {
                        if (n instanceof Button) {
                            Button b = (Button) n;
                            String t = b.getText();
                            if (t != null && t.length() > 0 && t.charAt(0) == letter) {
                                b.setStyle("-fx-background-color: linear-gradient(#e74c3c, #999999); -fx-text-fill: white;");
                                break;
                            }
                        }
                    }
                });
                if (model.getMistakes() <= 6) {
                    shakeOnWrong();
                }
            }
            updateMistakeVideo();
        } else {
            playRightSound();
            // correct guess: mark the pressed key as correct (green-gray)
            Platform.runLater(() -> {
                for (javafx.scene.Node n : keyboardPane.getChildren()) {
                    if (n instanceof Button) {
                        Button b = (Button) n;
                        String t = b.getText();
                        if (t != null && t.length() > 0 && t.charAt(0) == letter) {
                            b.setStyle("-fx-background-color: linear-gradient(#2ecc71, #99a99a); -fx-text-fill: white;");
                            break;
                        }
                    }
                }
            });
        }

        
        if (model.isVictory()) {
            showEndGame(true);
        } else if (model.isGameOver() && model.getMistakes() < 10) {
            showEndGame(false);
        }
    }

    @FXML
    private void handleBuyHint() {
        Difficulty difficulty = GameSettings.getDifficulty();
        int hintCost = GameSettings.getHintCost(difficulty);
        if (model == null || GameSettings.getFishBalance(difficulty) < hintCost || !model.hasHiddenLetters()) {
            return;
        }

        if (!model.revealHint()) {
            return;
        }

        GameSettings.spendFish(difficulty, hintCost);
        playBuyHintSound();
        updateWordDisplay();
        updateFishButton();
    }

    private void updateWordDisplay() {
        wordLabel.setText(model.getDisplayWord());
    }

    private void showEndGame(boolean isVictory) {
        GameSettings.addFish(GameSettings.getDifficulty(), currentRoundFish);
        updateFishButton();
        // Prepare result label immediately
        if (isVictory) {
            resultLabel.setText("VICTORY!");
            resultLabel.setTextFill(Color.web("#2ecc71"));
        } else {
            resultLabel.setText("GAME OVER");
            resultLabel.setTextFill(Color.web("#e74c3c"));
        }

        // Fade the full word first, then show overlay and start video
        // Fade out game sound before showing overlays for a smoother transition
        fadeOutGameSound();

        // Fade the word, then wait a bit before showing the overlay and starting the video
        fadeInFullWord(() -> {
            PauseTransition overlayDelay = new PauseTransition(javafx.util.Duration.millis(1200));
            overlayDelay.setOnFinished(ev -> {
                BoxBlur blur = new BoxBlur(10, 10, 3);
                gameBoard.setEffect(blur);
                if (keyboardPane != null) {
                    keyboardPane.setDisable(true);
                }
                if (homeButton != null) {
                    homeButton.setDisable(false);
                }

                playTryAgainVideo(isVictory);

                overlayPane.setVisible(true);
                overlayPane.setManaged(true);
                overlayPane.setMouseTransparent(false);
            });
            overlayDelay.play();
        });
    }

    @FXML
    private void handleRestart() {
        playButtonSound();
        stopTryAgainAudio();
        gameBoard.setEffect(null);
        gameBoard.setDisable(false);
        if (homeButton != null) {
            homeButton.setDisable(false);
        }
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
        overlayPane.setMouseTransparent(true);
        startNewGame();
    }
@FXML
    private void handleReturnHome(javafx.event.ActionEvent event) {
        if (mistakePlayer != null) {
            try {
                mistakePlayer.stop();
                mistakePlayer.dispose();
            } catch (Exception ignored) {}
        }
        playButtonSound();
        stopTryAgainAudio();
        if (gameSoundPlayer != null) {
            try { gameSoundPlayer.stop(); gameSoundPlayer.dispose(); } catch (Exception ignored) {}
            gameSoundPlayer = null;
        }
        SceneNavigator.switchTo("menu-view.fxml");
    }

    private void playButtonSound() {
        if (buttonSoundMedia == null) return;
        try {
            MediaPlayer p = new MediaPlayer(buttonSoundMedia);
            p.setOnEndOfMedia(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
            p.setOnError(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
            p.play();
        } catch (Exception ignored) {}
    }

    public static void stopActiveGameSound() {
        if (activeInstance != null) {
            activeInstance.stopGameSound();
        }
    }

    private void fadeOutGameSound() {
        if (gameSoundPlayer == null) {
            return;
        }
        MediaPlayer player = gameSoundPlayer;
        gameSoundPlayer = null;
        fadeOutAndDispose(player, javafx.util.Duration.millis(800));
    }

    private void stopGameSound() {
        fadeOutGameSound();
    }

    private void fadeOutAndDispose(MediaPlayer player, javafx.util.Duration duration) {
        if (player == null) {
            return;
        }
        double startVolume = player.getVolume();
        if (startVolume <= 0.0) {
            try { player.stop(); player.dispose(); } catch (Exception ignored) {}
            return;
        }
        Timeline fade = new Timeline(
            new KeyFrame(javafx.util.Duration.ZERO, new KeyValue(player.volumeProperty(), startVolume)),
            new KeyFrame(duration, new KeyValue(player.volumeProperty(), 0.0))
        );
        fade.setOnFinished(evt -> {
            try { player.stop(); player.dispose(); } catch (Exception ignored) {}
        });
        fade.play();
    }
}
