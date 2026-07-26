package fr.quentincillierre.hangman.controller;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.MediaView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    @BeforeAll
    static void initToolkit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX toolkit should start");
    }

    @Test
    void showEndGameKeepsHomeButtonEnabled() throws Exception {
        GameController controller = new GameController();

        AnchorPane gameBoard = new AnchorPane();
        StackPane overlayPane = new StackPane();
        Label resultLabel = new Label();
        Label categoryLabel = new Label();
        Label wordLabel = new Label();
        TilePane keyboardPane = new TilePane();
        MediaView mediaView = new MediaView();
        Button homeButton = new Button("HOME");

        setField(controller, "gameBoard", gameBoard);
        setField(controller, "overlayPane", overlayPane);
        setField(controller, "resultLabel", resultLabel);
        setField(controller, "categoryLabel", categoryLabel);
        setField(controller, "wordLabel", wordLabel);
        setField(controller, "keyboardPane", keyboardPane);
        setField(controller, "hangmanMediaView", mediaView);
        setField(controller, "homeButton", homeButton);

        gameBoard.getChildren().add(homeButton);

        Method showEndGame = GameController.class.getDeclaredMethod("showEndGame", boolean.class);
        showEndGame.setAccessible(true);
        showEndGame.invoke(controller, true);

        assertFalse(gameBoard.isDisable(), "The game board should remain interactive so the home button stays clickable");
        assertFalse(homeButton.isDisabled(), "The home button should remain enabled after the game ends");
        assertTrue(overlayPane.isVisible(), "The end-of-game overlay should still appear");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
