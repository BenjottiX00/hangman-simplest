package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.SceneNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class IntroController implements Initializable {

    @FXML private ImageView introView;

    private Timeline introTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<Image> frames = loadFrames();
            if (!frames.isEmpty()) {
                introView.setImage(frames.get(0));
                introView.setFitHeight(600);
                introView.setFitWidth(900);
                introView.setPreserveRatio(false);

                final int[] index = {0};
                introTimeline = new Timeline(
                    new KeyFrame(Duration.millis(180), event -> {
                        index[0] = (index[0] + 1) % frames.size();
                        introView.setImage(frames.get(index[0]));
                    })
                );
                introTimeline.setCycleCount(frames.size());
                introTimeline.setOnFinished(event -> SceneNavigator.switchTo("game-view.fxml"));
                introTimeline.play();
                return;
            }
        } catch (Exception e) {
            System.err.println("FAILED TO LOAD INTRO IMAGE: " + e.getMessage());
        }

        SceneNavigator.switchTo("game-view.fxml");
    }

    private List<Image> loadFrames() {
        List<Image> frames = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            URL resource = getClass().getResource("/pictures/" + i + "-hangman.png");
            if (resource != null) {
                frames.add(new Image(resource.toExternalForm()));
            }
        }
        return frames;
    }
}