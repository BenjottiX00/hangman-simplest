package fr.quentincillierre.hangman.application;

import javafx.application.Platform;
import javafx.scene.media.Media;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public final class MediaLoader {

    private static final Map<String, Media> MEDIA_CACHE = new ConcurrentHashMap<>();

    private MediaLoader() {
    }

    public static Media load(String resourcePath) {
        return MEDIA_CACHE.computeIfAbsent(resourcePath, key -> createMedia(key));
    }

    private static Media createMedia(String resourcePath) {
        URL resource = resolveResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Media resource not found: " + resourcePath);
        }
        return new Media(resource.toExternalForm());
    }

    public static boolean exists(String resourcePath) {
        return resolveResource(resourcePath) != null;
    }

    public static void preload(String resourcePath) {
        load(resourcePath);
    }

    public static void preloadAll(Collection<String> resourcePaths) {
        for (String resourcePath : resourcePaths) {
            try {
                if (!exists(resourcePath)) {
                    System.err.println("Skipping missing preload resource: " + resourcePath);
                    continue;
                }

                // Validate media before caching to avoid invalid media causing runtime freezes
                boolean valid = validateMedia(resourcePath, 6);
                if (valid) {
                    preload(resourcePath);
                } else {
                    System.err.println("Media not ready yet, scheduling retries: " + resourcePath);
                    // Attempt to create a MediaPlayer with retries; when ready, cache the media via preload
                    createMediaPlayerWithRetries(resourcePath, 5, 2000,
                        mp -> {
                            try {
                                // Cache the underlying Media and dispose the temporary player
                                preload(resourcePath);
                            } catch (Exception ignored) {
                            } finally {
                                try { mp.stop(); mp.dispose(); } catch (Exception ignored) {}
                            }
                        },
                        err -> {
                            System.err.println("Preload retries failed for " + resourcePath + " -> " + err.getMessage());
                        }
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to preload media: " + resourcePath + " -> " + e.getMessage());
            }
        }
    }

    /**
     * Returns true if the given resource has been successfully preloaded and cached.
     */
    public static boolean isPreloaded(String resourcePath) {
        return MEDIA_CACHE.containsKey(resourcePath);
    }

    /**
     * Validate a media resource by creating a temporary MediaPlayer on the JavaFX thread
     * and waiting for either onReady or onError. Must NOT be called from the JavaFX thread.
     * Returns true if media is loadable and ready within the timeout.
     */
    public static boolean validateMedia(String resourcePath, int timeoutSeconds) {
        URL resource = resolveResource(resourcePath);
        if (resource == null) return false;

        final Media media = new Media(resource.toExternalForm());
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean ok = new AtomicBoolean(false);
        final javafx.scene.media.MediaPlayer[] holder = new javafx.scene.media.MediaPlayer[1];

        try {
            if (Platform.isFxApplicationThread()) {
                // Validation must not block the FX thread; bail out and assume valid so caller can handle errors.
                return true;
            }

            Platform.runLater(() -> {
                try {
                    javafx.scene.media.MediaPlayer mp = new javafx.scene.media.MediaPlayer(media);
                    holder[0] = mp;
                    mp.setMute(true);
                    mp.setOnError(() -> {
                        latch.countDown();
                    });
                    mp.setOnReady(() -> {
                        ok.set(true);
                        latch.countDown();
                    });
                    // start loading; some platforms require play() to progress loading
                    try { mp.play(); } catch (Exception ignored) {}
                } catch (Exception e) {
                    latch.countDown();
                }
            });

            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            // dispose player on FX thread
            Platform.runLater(() -> {
                try {
                    if (holder[0] != null) {
                        holder[0].stop();
                        holder[0].dispose();
                    }
                } catch (Exception ignored) {}
            });

            return completed && ok.get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    public static List<String> listResources(String directory, String suffix) {
        List<String> results = new ArrayList<>();
        String normalizedDir = directory.startsWith("/") ? directory.substring(1) : directory;
        URL dirURL = MediaLoader.class.getResource("/" + normalizedDir);
        if (dirURL == null) {
            return results;
        }

        try {
            if ("file".equals(dirURL.getProtocol())) {
                Path dir = Paths.get(dirURL.toURI());
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*" + suffix)) {
                    for (Path entry : stream) {
                        if (Files.isRegularFile(entry)) {
                            results.add(normalizedDir + "/" + entry.getFileName().toString());
                        }
                    }
                }
            } else if ("jar".equals(dirURL.getProtocol())) {
                JarURLConnection jarConn = (JarURLConnection) dirURL.openConnection();
                try (JarFile jar = jarConn.getJarFile()) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(normalizedDir + "/") && name.endsWith(suffix) && !entry.isDirectory()) {
                            results.add(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not list resources for " + directory + ": " + e.getMessage());
        }
        return results;
    }

    /**
     * Create a MediaPlayer for the given resource, retrying a few times on error.
     * The callbacks are invoked on the JavaFX thread.
     */
    public static void createMediaPlayerWithRetries(String resourcePath, int retries, int delayMs,
                                                    Consumer<javafx.scene.media.MediaPlayer> onReady,
                                                    Consumer<Throwable> onFailure) {
        Consumer<Integer> attempt = new Consumer<>() {
            @Override
            public void accept(Integer attemptsLeft) {
                Platform.runLater(() -> {
                    try {
                        Media media = load(resourcePath);
                        javafx.scene.media.MediaPlayer mp = new javafx.scene.media.MediaPlayer(media);
                        // mute until caller decides to play
                        mp.setMute(true);
                        mp.setOnError(() -> {
                            Throwable err = mp.getError();
                            try { mp.stop(); mp.dispose(); } catch (Exception ignored) {}
                            if (attemptsLeft > 0) {
                                PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
                                pause.setOnFinished(e -> accept(attemptsLeft - 1));
                                pause.play();
                            } else {
                                onFailure.accept(err != null ? err : new RuntimeException("Unknown media error"));
                            }
                        });
                        mp.setOnReady(() -> {
                            // give caller the ready player; caller can unmute/play
                            try { mp.setMute(false); } catch (Exception ignored) {}
                            onReady.accept(mp);
                        });
                        // start load
                        try { mp.play(); mp.pause(); } catch (Exception ignored) {}
                    } catch (Exception ex) {
                        if (attemptsLeft > 0) {
                            PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
                            pause.setOnFinished(e -> accept(attemptsLeft - 1));
                            pause.play();
                        } else {
                            onFailure.accept(ex);
                        }
                    }
                });
            }
        };

        attempt.accept(retries);
    }

    private static URL resolveResource(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        URL resource = MediaLoader.class.getResource(normalized);
        if (resource == null) {
            resource = MediaLoader.class.getClassLoader().getResource(normalized.startsWith("/") ? normalized.substring(1) : normalized);
        }

        if (resource == null) {
            resource = MediaLoader.class.getResource("application/" + resourcePath);
        }

        // Fallback: if resource not found and it's in the videos folder, look for a fixed re-encoded copy
        if (resource == null && resourcePath.startsWith("videos/")) {
            String filename = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            String fixedPath = "/videos/fixed/" + filename.replaceFirst("\\.mp4$", "-fixed.mp4");
            URL fixed = MediaLoader.class.getResource(fixedPath);
            if (fixed != null) {
                System.err.println("Using fixed re-encoded media for " + resourcePath + " -> " + fixedPath);
                resource = fixed;
            }
        }

        return resource;
    }
}