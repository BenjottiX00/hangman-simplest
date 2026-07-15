package fr.quentincillierre.hangman.application;

import fr.quentincillierre.hangman.controller.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));

        Parent root = loader.load();

        GameController controller = loader.getController();

        Scene scene = new Scene(root, 800, 800);

        scene.setOnKeyTyped(event -> {
            controller.handleKeyboardInput(event.getCharacter());
        });

        primaryStage.setTitle("HangMan");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
