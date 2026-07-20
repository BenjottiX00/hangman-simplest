package fr.quentincillierre.hangman.application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Small helper that lets any controller switch the primary stage to a
 * different FXML screen (menu -> intro -> game) without each controller
 * needing its own reference to the Stage.
 */
public final class SceneNavigator {

    private static Stage stage;

    private SceneNavigator() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /**
     * Loads the given FXML file (resolved relative to the application
     * package, same folder as MainApp) and swaps it into the current stage.
     */
    public static void switchTo(String fxmlFileName) {
        if (stage == null) {
            throw new IllegalStateException("SceneNavigator.init(stage) was never called.");
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlFileName));
            Parent root = loader.load();

            double width = stage.getScene() != null ? stage.getScene().getWidth() : 900;
            double height = stage.getScene() != null ? stage.getScene().getHeight() : 600;

            stage.setScene(new Scene(root, width, height));
        } catch (Exception e) {
            throw new RuntimeException("Failed to switch to view: " + fxmlFileName, e);
        }
    }
}
