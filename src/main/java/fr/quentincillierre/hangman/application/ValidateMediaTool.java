package fr.quentincillierre.hangman.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.List;

public class ValidateMediaTool extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Thread t = new Thread(() -> {
            try {
                List<String> videos = MediaLoader.listResources("videos", ".mp4");
                System.out.println("Found " + videos.size() + " mp4 files to validate.");
                for (String v : videos) {
                    System.out.print("Validating: " + v + " ... ");
                    boolean ok = MediaLoader.validateMedia(v, 6);
                    System.out.println(ok ? "OK" : "INVALID");
                }
            } catch (Exception e) {
                System.err.println("Validation failed: " + e.getMessage());
            } finally {
                Platform.exit();
            }
        }, "MediaValidatorThread");
        t.setDaemon(true);
        t.start();
    }
}
