package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ResourceBundle;

public class IntroController implements Initializable {
    @FXML private MediaView introView;
    private MediaPlayer introPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (introView == null) {
                System.err.println("IntroController: introView not injected");
                disposeAndSwitchToGame();
                return;
            }

            Media media = MediaLoader.load("intro-2.mp4");

            introPlayer = new MediaPlayer(media);
            introView.setMediaPlayer(introPlayer);

            introPlayer.setOnEndOfMedia(this::disposeAndSwitchToGame);

            introPlayer.setOnError(() -> {
                System.err.println("Intro media player error: " + introPlayer.getError());
                disposeAndSwitchToGame();
            });

            // Play only once the media is ready to avoid instant end/zero-duration issues
            introPlayer.setOnReady(() -> {
                try {
                    introPlayer.play();
                } catch (Exception e) {
                    System.err.println("Failed to play intro video: " + e.getMessage());
                    disposeAndSwitchToGame();
                }
            });

            // If the scene containing this view is removed (switching scenes), dispose resources
            introView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) {
                    if (introPlayer != null) {
                        try {
                            introPlayer.stop();
                            introPlayer.dispose();
                        } catch (Exception ignored) {}
                        try { introView.setMediaPlayer(null); } catch (Exception ignored) {}
                        introPlayer = null;
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Could not load intro video, skipping to game: " + e.getMessage());
            e.printStackTrace();
            disposeAndSwitchToGame();
        }
    }

    private void disposeAndSwitchToGame() {
        if (introPlayer != null) {
            try {
                introPlayer.stop();
                introPlayer.dispose();
            } catch (Exception ignored) {}
        }
        try { introView.setMediaPlayer(null); } catch (Exception ignored) {}
        introPlayer = null;

        Platform.runLater(() -> {
            SceneNavigator.switchTo("game-view.fxml");
        });
    }
}
