package fr.quentincillierre.hangman.application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {
    private static final String VIEW_PACKAGE = "/fr/quentincillierre/hangman/application/";
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlFile) {
        if (primaryStage == null) {
            return;
        }

        Platform.runLater(() -> {
            try {
                URL fxmlUrl = resolveResource(fxmlFile);
                if (fxmlUrl == null) {
                    throw new IOException("FXML resource not found: " + fxmlFile);
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Scene scene = new Scene(root, 900, 600);

                URL cssUrl = resolveResource(getCssFileName(fxmlFile));
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }

                primaryStage.setScene(scene);
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Could not navigate to: " + fxmlFile);
            }
        });
    }

    private static URL resolveResource(String resourceName) {
        if (resourceName == null || resourceName.isBlank()) {
            return null;
        }

        String normalized = resourceName.startsWith("/") ? resourceName : "/" + resourceName;
        URL resource = SceneNavigator.class.getResource(normalized);
        if (resource != null) {
            return resource;
        }

        return SceneNavigator.class.getResource(VIEW_PACKAGE + resourceName);
    }

    private static String getCssFileName(String fxmlFile) {
        if (fxmlFile == null) {
            return null;
        }
        if (fxmlFile.contains("menu")) {
            return "menu.css";
        }
        if (fxmlFile.contains("game")) {
            return "game.css";
        }
        return null;
    }
}