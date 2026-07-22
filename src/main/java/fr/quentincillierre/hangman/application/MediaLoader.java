package fr.quentincillierre.hangman.application;

import javafx.scene.media.Media;

import java.net.URL;

public final class MediaLoader {

    private MediaLoader() {
    }

    public static Media load(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        URL resource = MediaLoader.class.getResource(normalized);
        if (resource == null) {
            resource = MediaLoader.class.getClassLoader().getResource(normalized.startsWith("/") ? normalized.substring(1) : normalized);
        }
        if (resource == null) {
            throw new IllegalArgumentException("Media resource not found: " + resourcePath);
        }

        return new Media(resource.toExternalForm());
    }
}
