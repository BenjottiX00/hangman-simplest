package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.SceneNavigator;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.GameSettings;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML private ImageView backgroundView;
    @FXML private Button btnEasy;
    @FXML private Button btnMedium;
    @FXML private Button btnHard;

    private Timeline backgroundTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/PermanentMarker-Regular.ttf"), 14);

        try {
            List<Image> frames = loadFrames();
            if (!frames.isEmpty()) {
                backgroundView.setImage(frames.get(0));
                backgroundView.setFitHeight(600);
                backgroundView.setFitWidth(900);
                backgroundView.setPreserveRatio(false);

                final int[] index = {0};
                backgroundTimeline = new Timeline(
                    new KeyFrame(Duration.millis(180), event -> {
                        index[0] = (index[0] + 1) % frames.size();
                        backgroundView.setImage(frames.get(index[0]));
                    })
                );
                backgroundTimeline.setCycleCount(Timeline.INDEFINITE);
                backgroundTimeline.play();
            }
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }

        GameSettings.setDifficulty(Difficulty.EASY);
        updateButtonStyles();
    }

    @FXML
    private void setEasy() {
        GameSettings.setDifficulty(Difficulty.EASY);
        updateButtonStyles();
    }

    @FXML
    private void setMedium() {
        GameSettings.setDifficulty(Difficulty.MEDIUM);
        updateButtonStyles();
    }

    @FXML
    private void setHard() {
        GameSettings.setDifficulty(Difficulty.HARD);
        updateButtonStyles();
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
        if (backgroundTimeline != null) {
            backgroundTimeline.stop();
        }
        SceneNavigator.switchTo("intro-view.fxml");
    }

    private List<Image> loadFrames() {
        List<Image> frames = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            URL resource = getClass().getResource("/pictures/" + i + "-hangman.png");
            if (resource != null) {
                frames.add(new Image(resource.toExternalForm()));
            }
        }
        return frames;
    }
}