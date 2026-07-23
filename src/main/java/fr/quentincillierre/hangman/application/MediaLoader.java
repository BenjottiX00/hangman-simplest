package fr.quentincillierre.hangman.application;

import javafx.scene.media.Media;
import java.net.URL;

public final class MediaLoader {

    private MediaLoader() {
    }

    public static Media load(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        
        // Look directly in the classpath root (where resource files like intro-2.mp4 are placed)
        URL resource = MediaLoader.class.getResource(normalized);
        if (resource == null) {
            resource = MediaLoader.class.getClassLoader().getResource(normalized.startsWith("/") ? normalized.substring(1) : normalized);
        }
        
        // Fallback: check inside the fr/quentincillierre/hangman/application package resources if needed
        if (resource == null) {
            resource = MediaLoader.class.getResource("application/" + resourcePath);
        }

        if (resource == null) {
            throw new IllegalArgumentException("Media resource not found: " + resourcePath);
        }

        return new Media(resource.toExternalForm());
    }
}