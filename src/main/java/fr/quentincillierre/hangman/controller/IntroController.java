package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ResourceBundle;

public class IntroController implements Initializable {

    @FXML private MediaView introView;
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mediaPlayer = new MediaPlayer(MediaLoader.load("intro-2.mp4"));
        introView.setMediaPlayer(mediaPlayer);

        // Fixes whitespace by covering the 900x600 window completely
        introView.setFitHeight(600);
        introView.setPreserveRatio(true); 

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.dispose();
            SceneNavigator.switchTo("game-view.fxml");
        });

        mediaPlayer.play();
    }
}