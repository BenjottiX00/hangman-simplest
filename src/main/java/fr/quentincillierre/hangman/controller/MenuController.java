package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.GameSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML private MediaView backgroundView;
    @FXML private Button btnEasy;
    @FXML private Button btnMedium;
    @FXML private Button btnHard;

    private MediaPlayer mediaPlayer;
    private MediaPlayer menuSoundPlayer;
    private javafx.scene.media.Media buttonSoundMedia;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/PermanentMarker-Regular.ttf"), 14);

        try {
            MediaLoader.createMediaPlayerWithRetries("videos/menu.mp4", 2, 1000, mp -> {
                try {
                    mediaPlayer = mp;
                    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    backgroundView.setMediaPlayer(mediaPlayer);
                    mediaPlayer.play();
                } catch (Exception ignored) {}
            }, err -> System.err.println("Could not load background video: " + err));
        } catch (Exception e) {
            System.err.println("Could not load background video: " + e.getMessage());
        }

        // Prepare the button-press sound: prefer soundEffects/menuButton.mp3, fallback to menuButtons_soundtrack
        String preferred = "soundEffects/menuButton.mp3";
        String fallback = "soundEffects/menuButtons_soundtrack.mp3";
        try {
            String chosen = MediaLoader.exists(preferred) ? preferred : (MediaLoader.exists(fallback) ? fallback : null);
            if (chosen != null) {
                // preload and load media for quick playback
                try { MediaLoader.preload(chosen); } catch (Exception ignored) {}
                try {
                    buttonSoundMedia = MediaLoader.load(chosen);
                } catch (Exception ex) {
                    System.err.println("Could not load button sound media: " + ex.getMessage());
                    buttonSoundMedia = null;
                }
            }
        } catch (Exception ex) {
            System.err.println("Error preparing button sound: " + ex.getMessage());
        }

        // Play looping menu sound for menu stage only
        try {
            MediaLoader.createMediaPlayerWithRetries("soundEffects/MenuSound.mp3", 2, 500, sp -> {
                try {
                    menuSoundPlayer = sp;
                    menuSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    menuSoundPlayer.setVolume(0.8);
                    menuSoundPlayer.play();
                } catch (Exception ignored) {}
            }, err -> System.err.println("Could not load menu sound: " + err));
        } catch (Exception e) {
            System.err.println("Could not load menu sound: " + e.getMessage());
        }

        GameSettings.setDifficulty(Difficulty.EASY);
        updateButtonStyles();
    }

    @FXML private void setEasy() { playButtonSound(); GameSettings.setDifficulty(Difficulty.EASY); updateButtonStyles(); }
    @FXML private void setMedium() { playButtonSound(); GameSettings.setDifficulty(Difficulty.MEDIUM); updateButtonStyles(); }
    @FXML private void setHard() { playButtonSound(); GameSettings.setDifficulty(Difficulty.HARD); updateButtonStyles(); }

    private void playButtonSound() {
        if (buttonSoundMedia == null) return;
        try {
            javafx.scene.media.MediaPlayer p = new javafx.scene.media.MediaPlayer(buttonSoundMedia);
            p.setOnEndOfMedia(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
            p.setOnError(() -> { try { p.stop(); p.dispose(); } catch (Exception ignored) {} });
            p.play();
        } catch (Exception ignored) {}
    }

    private void updateButtonStyles() {
        btnEasy.setStyle("-fx-border-color: black; -fx-opacity: 0.5;");
        btnMedium.setStyle("-fx-border-color: black; -fx-opacity: 0.5;");
        btnHard.setStyle("-fx-border-color: black; -fx-opacity: 0.5;");

        Difficulty current = GameSettings.getDifficulty();
        if (current == Difficulty.EASY) btnEasy.setStyle("-fx-border-color: white; -fx-opacity: 1.0;");
        else if (current == Difficulty.MEDIUM) btnMedium.setStyle("-fx-border-color: white; -fx-opacity: 1.0;");
        else if (current == Difficulty.HARD) btnHard.setStyle("-fx-border-color: white; -fx-opacity: 1.0;");
    }

    @FXML
    private void handleStart() {
        playButtonSound();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        if (menuSoundPlayer != null) {
            fadeOutAndDispose(menuSoundPlayer, javafx.util.Duration.millis(800));
            menuSoundPlayer = null;
        }
        SceneNavigator.switchTo("intro-view.fxml");
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