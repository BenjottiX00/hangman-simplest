package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.SceneNavigator;
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

        String url = getClass().getResource(INTRO_CLIPS[index]).toExternalForm();
        Media media = new Media(url);

        currentPlayer = new MediaPlayer(media);
        introView.setMediaPlayer(currentPlayer);
        currentPlayer.setOnEndOfMedia(() -> playClip(index + 1));
        currentPlayer.play();
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
