package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.MediaLoader;
import fr.quentincillierre.hangman.application.SceneNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
 
import javafx.animation.PauseTransition;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class IntroController implements Initializable {
    @FXML private MediaView introView;
    @FXML private javafx.scene.control.Button skipButton;
    private MediaPlayer loadingPlayer;
    private MediaPlayer introPlayer;
    private volatile boolean preloadFinished = false;
    private volatile boolean introStarted = false;
    private volatile boolean loadingPlayable = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (introView == null) {
                System.err.println("IntroController: introView not injected");
                disposeAndSwitchToGame();
                return;
            }

            if (skipButton != null) {
                skipButton.setDisable(true);
            }

            startPreloadTask();
            startLoadingVideo();

            // If the scene containing this view is removed (switching scenes), dispose resources
            introView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) {
                    if (loadingPlayer != null) {
                        try {
                            loadingPlayer.stop();
                            loadingPlayer.dispose();
                        } catch (Exception ignored) {}
                        loadingPlayer = null;
                    }
                    if (introPlayer != null) {
                        try {
                            introPlayer.stop();
                            introPlayer.dispose();
                        } catch (Exception ignored) {}
                        introPlayer = null;
                    }
                    try { introView.setMediaPlayer(null); } catch (Exception ignored) {}
                }
            });
        } catch (Exception e) {
            System.err.println("Could not load loading video, skipping to game: " + e.getMessage());
            e.printStackTrace();
            disposeAndSwitchToGame();
        }
    }

    private void startLoadingVideo() {
        try {
            if (!MediaLoader.exists("videos/loading.mp4")) {
                System.err.println("Loading video not found, skipping loading video.");
                return;
            }

            MediaLoader.createMediaPlayerWithRetries("videos/loading.mp4", 2, 1000, mp -> {
                try {
                    loadingPlayer = mp;
                    loadingPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    introView.setMediaPlayer(loadingPlayer);
                    loadingPlayable = true;
                    loadingPlayer.play();
                } catch (Exception e) {
                    System.err.println("Failed to play loading video: " + e.getMessage());
                    loadingPlayable = false;
                    if (preloadFinished) startIntroVideo();
                }
            }, err -> {
                System.err.println("Loading video error: " + err);
                loadingPlayable = false;
                if (preloadFinished) startIntroVideo();
            });

            // Watchdog: if loading video doesn't start playing within 3s, proceed to intro when possible
            PauseTransition watchdog = new PauseTransition(javafx.util.Duration.seconds(3));
            watchdog.setOnFinished(evt -> {
                if (!loadingPlayable) {
                    System.err.println("Loading video did not start within timeout, will proceed when preload completes.");
                    if (preloadFinished) startIntroVideo();
                }
            });
            watchdog.play();
        } catch (Exception e) {
            System.err.println("Could not create loading player: " + e.getMessage());
        }
    }

    private void startPreloadTask() {
        Task<Void> preloadTask = new Task<>() {
            @Override
            protected Void call() {
                List<String> paths = new ArrayList<>();
                paths.add("videos/loading.mp4");
                paths.add("videos/menu.mp4");
                paths.add("videos/victoryTryAgain.mp4");
                paths.add("videos/defeatTryAgain.mp4");
                paths.add("videos/intro video.mp4");
                paths.addAll(MediaLoader.listResources("videos", ".mp4"));
                MediaLoader.preloadAll(paths);
                return null;
            }
        };

        preloadTask.setOnSucceeded(evt -> {
            preloadFinished = true;
            Platform.runLater(() -> {
                if (!introStarted) {
                    startIntroVideo();
                }
            });
        });

        preloadTask.setOnFailed(evt -> {
            preloadFinished = true;
            System.err.println("Preload task failed: " + preloadTask.getException());
            Platform.runLater(() -> {
                if (!introStarted) {
                    startIntroVideo();
                }
            });
        });

        Thread preloadThread = new Thread(preloadTask, "MediaPreloadThread");
        preloadThread.setDaemon(true);
        preloadThread.start();
    }

    @FXML
    private void handleSkipIntro() {
        if (!introStarted) {
            return;
        }
        disposeAndSwitchToGame();
    }

    private void startIntroVideo() {
        if (introStarted) {
            return;
        }

        introStarted = true;
        if (skipButton != null) {
            skipButton.setDisable(false);
        }

        if (loadingPlayer != null) {
            try {
                loadingPlayer.stop();
                loadingPlayer.dispose();
            } catch (Exception ignored) {}
            loadingPlayer = null;
        }

        try {
            MediaLoader.createMediaPlayerWithRetries("videos/intro video.mp4", 2, 1000, mp -> {
                try {
                    introPlayer = mp;
                    introPlayer.setCycleCount(1);
                    introView.setMediaPlayer(introPlayer);
                    introPlayer.play();
                    introPlayer.setOnEndOfMedia(this::disposeAndSwitchToGame);
                } catch (Exception e) {
                    System.err.println("Failed to play intro video: " + e.getMessage());
                    disposeAndSwitchToGame();
                }
            }, err -> {
                System.err.println("Intro video error: " + err);
                disposeAndSwitchToGame();
            });
        } catch (Exception e) {
            System.err.println("Could not load intro video: " + e.getMessage());
            disposeAndSwitchToGame();
        }
    }

    private void disposeAndSwitchToGame() {
        if (loadingPlayer != null) {
            try {
                loadingPlayer.stop();
                loadingPlayer.dispose();
            } catch (Exception ignored) {}
        }
        if (introPlayer != null) {
            try {
                introPlayer.stop();
                introPlayer.dispose();
            } catch (Exception ignored) {}
        }
        try { introView.setMediaPlayer(null); } catch (Exception ignored) {}
        loadingPlayer = null;
        introPlayer = null;

        Platform.runLater(() -> {
            SceneNavigator.switchTo("game-view.fxml");
        });
    }
}
