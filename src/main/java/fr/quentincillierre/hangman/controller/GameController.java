package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import fr.quentincillierre.hangman.model.GameSettings;
import fr.quentincillierre.hangman.model.HangmanModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML private AnchorPane gameBoard;
    @FXML private StackPane overlayPane;
    @FXML private Label categoryLabel;
    @FXML private Label wordLabel;
    @FXML private Label resultLabel;
    @FXML private TilePane keyboardPane;
    @FXML private MediaView hangmanMediaView;
    @FXML private Button homeButton;

    private HangmanModel model;
    private MediaPlayer mistakePlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/PermanentMarker-Regular.ttf"), 14);
        startNewGame();
    }

    private void startNewGame() {
        model = new HangmanModel(GameSettings.getDifficulty());
        categoryLabel.setText(model.getCategory());
        updateWordDisplay();
        setupKeyboard();
        updateMistakeVideo();
    }

    private void updateMistakeVideo() {
        int mistakes = model.getMistakes();
        String videoFile = String.format("videos/catvideo (%d).mp4", mistakes);
        
        try {
            if (mistakePlayer != null) {
                mistakePlayer.stop();
                mistakePlayer.dispose();
            }
            
            mistakePlayer = new MediaPlayer(MediaLoader.load(videoFile));
            mistakePlayer.setCycleCount(MediaPlayer.INDEFINITE);
            hangmanMediaView.setMediaPlayer(mistakePlayer);
            mistakePlayer.play();
        } catch (Exception e) {
            System.err.println("Could not load mistake video " + videoFile + ": " + e.getMessage());
        }
    }

    private void setupKeyboard() {
        keyboardPane.getChildren().clear();
        for (char c = 'A'; c <= 'Z'; c++) {
            Button keyBtn = new Button(String.valueOf(c));
            keyBtn.getStyleClass().add("keyboard-button");
            keyBtn.setPrefSize(45, 45);
            
            keyBtn.setOnAction(e -> {
                keyBtn.setDisable(true);
                handleGuess(keyBtn.getText().charAt(0));
            });
            keyboardPane.getChildren().add(keyBtn);
        }
    }

    private void handleGuess(char letter) {
        model.guessLetter(letter);
        updateWordDisplay();
        updateMistakeVideo();
        
        if (model.isVictory()) {
            showEndGame(true);
        } else if (model.isGameOver()) {
            showEndGame(false);
        }
    }

    private void updateWordDisplay() {
        wordLabel.setText(model.getDisplayWord());
    }

    private void showEndGame(boolean isVictory) {
        BoxBlur blur = new BoxBlur(10, 10, 3);
        gameBoard.setEffect(blur);
        gameBoard.setDisable(false);
        if (homeButton != null) {
            homeButton.setDisable(false);
        }

        if (isVictory) {
            resultLabel.setText("VICTORY!");
            resultLabel.setTextFill(Color.web("#2ecc71")); 
        } else {
            resultLabel.setText("GAME OVER\nWORD: " + model.getFullWord());
            resultLabel.setTextFill(Color.web("#e74c3c")); 
        }

        overlayPane.setVisible(true);
        overlayPane.setManaged(true);
    }

    @FXML
    private void handleRestart() {
        gameBoard.setEffect(null);
        gameBoard.setDisable(false);
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
        startNewGame();
    }
@FXML
    private void handleReturnHome(javafx.event.ActionEvent event) {
        if (mistakePlayer != null) {
            try {
                mistakePlayer.stop();
                mistakePlayer.dispose();
            } catch (Exception ignored) {}
        }
        SceneNavigator.switchTo("menu-view.fxml");
    }
}