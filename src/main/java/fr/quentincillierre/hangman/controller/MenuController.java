package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.SceneNavigator;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.GameSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML private MediaView backgroundView;
    @FXML private Button btnEasy;
    @FXML private Button btnMedium;
    @FXML private Button btnHard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load font programmatically to bypass JavaFX CSS module issues
        Font.loadFont(getClass().getResourceAsStream("/fonts/PermanentMarker-Regular.ttf"), 14);

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
        SceneNavigator.switchTo("intro-view.fxml");
    }
}