package fr.quentincillierre.hangman.application;

import javafx.scene.media.Media;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

        try {
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                return new Media(resource.toExternalForm());
            }

            Path tempFile = Files.createTempFile("jfx-media-", getFileName(normalized));
            tempFile.toFile().deleteOnExit();
            try (InputStream in = resource.openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return new Media(tempFile.toUri().toString());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load media resource: " + resourcePath, e);
        }
    }

    private static String getFileName(String resourcePath) {
        int lastSlash = resourcePath.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? resourcePath.substring(lastSlash + 1) : resourcePath;
        if (fileName.isEmpty()) {
            fileName = "media";
        }
        return fileName;
    }
}
