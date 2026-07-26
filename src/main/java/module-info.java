module fr.quentincillierre.hangman {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires transitive javafx.media;

    exports fr.quentincillierre.hangman.application;
    opens fr.quentincillierre.hangman.application to javafx.fxml;

    exports fr.quentincillierre.hangman.controller;
    opens fr.quentincillierre.hangman.controller to javafx.fxml;

    exports fr.quentincillierre.hangman.model;
}