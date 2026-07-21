package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class MenuController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private MediaView backgroundView;

    private MediaPlayer backgroundPlayer;

    @FXML
    public void initialize() {
        var cssUrl = getClass().getClassLoader().getResource("fr/quentincillierre/hangman/application/menu.css");
        if (cssUrl != null && rootPane != null) {
            rootPane.getStylesheets().add(cssUrl.toExternalForm());
        }

        try {
            Media media = MediaLoader.load("videos/menu-background.mp4");
            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundPlayer.setMute(true);
            backgroundPlayer.setOnError(() ->
                    System.out.println("Background video error: " + backgroundPlayer.getError()));
            backgroundPlayer.setOnReady(() -> Platform.runLater(() -> {
                System.out.println("Background video ready");
                if (backgroundView != null) {
                    backgroundView.setMediaPlayer(backgroundPlayer);
                }
                backgroundPlayer.play();
            }));
            backgroundPlayer.setOnPlaying(() -> System.out.println("Background video playing"));
            backgroundPlayer.setOnStalled(() -> System.out.println("Background video stalled"));
        } catch (Exception ex) {
            System.out.println("Background video could not be loaded: " + ex.getMessage());
        }
    }

    @FXML
    private void handleStart() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
        }
        SceneNavigator.switchTo("intro-view.fxml");
    }
}