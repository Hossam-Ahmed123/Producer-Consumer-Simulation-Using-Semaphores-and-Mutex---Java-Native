package com.os.producerconsumer.util;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging timestamped messages to the UI's TextArea.
 * Ensures all log updates run on the JavaFX Application Thread via Platform.runLater().
 */
public class UiLogger {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final TextArea logArea;

    public UiLogger(TextArea logArea) {
        this.logArea = logArea;
    }

    /**
     * Appends a timestamped message to the log area.
     * Thread-safe: uses Platform.runLater() to update UI from any thread.
     */
    public void log(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalTime.now().format(TIME_FORMATTER);
            logArea.appendText("[" + timestamp + "] " + message + "\n");
            // Auto-scroll to the bottom
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    /**
     * Clears all log entries.
     */
    public void clear() {
        Platform.runLater(logArea::clear);
    }
}
