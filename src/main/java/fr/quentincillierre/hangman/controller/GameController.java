package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.SceneNavigator;
import fr.quentincillierre.hangman.model.GameSettings;
import fr.quentincillierre.hangman.model.HangmanModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML private AnchorPane gameBoard;
    @FXML private StackPane overlayPane;
    @FXML private Label categoryLabel;
    @FXML private Label wordLabel;
    @FXML private Label resultLabel;
    @FXML private TilePane keyboardPane;
    @FXML private ImageView hangmanMediaView;

    private HangmanModel model;
    private List<Image> hangmanFrames = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/PermanentMarker-Regular.ttf"), 14);

        try {
            hangmanFrames = loadFrames();
            if (!hangmanFrames.isEmpty()) {
                hangmanMediaView.setImage(hangmanFrames.get(0));
                hangmanMediaView.setFitHeight(260);
                hangmanMediaView.setFitWidth(320);
                hangmanMediaView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load game image: " + e.getMessage());
        }

        startNewGame();
    }

    private void startNewGame() {
        model = new HangmanModel(GameSettings.getDifficulty());
        categoryLabel.setText(model.getCategory());
        updateWordDisplay();
        setupKeyboard();
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

        if (!hangmanFrames.isEmpty()) {
            int mistakeIndex = Math.min(model.getMistakes(), hangmanFrames.size() - 1);
            hangmanMediaView.setImage(hangmanFrames.get(mistakeIndex));
        }
        
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
        gameBoard.setDisable(true);

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
    private void handleReturnHome() {
        SceneNavigator.switchTo("menu-view.fxml");
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