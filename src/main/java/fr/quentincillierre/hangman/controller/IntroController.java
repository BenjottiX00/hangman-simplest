package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class IntroController {

    // Played back to back, in this order. To change the intro sequence,
    // just edit this list - no other code needs to change.
    private static final String[] INTRO_CLIPS = {
            "/videos/intro-1.mp4", // Untitled_design
            "/videos/intro-2.mp4"  // Greeting_Exchange
    };

    @FXML
    private MediaView introView;

    private MediaPlayer currentPlayer;

    @FXML
    public void initialize() {
        playClip(0);
    }

    private void playClip(int index) {
        if (index >= INTRO_CLIPS.length) {
            goToGame();
            return;
        }

        Media media = MediaLoader.load(INTRO_CLIPS[index]);

        currentPlayer = new MediaPlayer(media);
        currentPlayer.setOnError(() ->
                System.out.println("Intro video error: " + currentPlayer.getError()));
        currentPlayer.setOnReady(() -> Platform.runLater(() -> {
            System.out.println("Intro video ready: index=" + index);
            if (introView != null) {
                introView.setMediaPlayer(currentPlayer);
            }
            currentPlayer.setOnEndOfMedia(() -> playClip(index + 1));
            currentPlayer.play();
        }));
        currentPlayer.setOnPlaying(() -> System.out.println("Intro video playing: index=" + index));
        currentPlayer.setOnStalled(() -> System.out.println("Intro video stalled: index=" + index));
    }

    @FXML
    private void handleSkip() {
        if (currentPlayer != null) {
            currentPlayer.stop();
        }
        goToGame();
    }

    private void goToGame() {
        SceneNavigator.switchTo("game-view.fxml");
    }
}
