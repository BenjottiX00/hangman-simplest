package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class MenuController {

    @FXML
    private MediaView backgroundView;

    private MediaPlayer backgroundPlayer;

    @FXML
    public void initialize() {
        String videoUrl = getClass().getResource("/videos/menu-background.mp4").toExternalForm();
        Media media = new Media(videoUrl);

        backgroundPlayer = new MediaPlayer(media);
        backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundPlayer.setMute(true); // ambient loop - no audio needed
        backgroundView.setMediaPlayer(backgroundPlayer);
        backgroundPlayer.play();
    }

    @FXML
    private void handleStart() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
        }
        SceneNavigator.switchTo("intro-view.fxml");
    }
}
