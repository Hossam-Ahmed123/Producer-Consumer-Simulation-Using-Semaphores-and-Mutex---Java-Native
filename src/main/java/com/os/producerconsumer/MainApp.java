package com.os.producerconsumer;

import com.os.producerconsumer.controller.SimulationController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main entry point for the Producer-Consumer Simulation application.
 *
 * This JavaFX application visually demonstrates the classic Operating Systems
 * synchronization problem using Semaphores and Mutex.
 *
 * How to run:
 *   mvn clean javafx:run
 *
 * Requirements:
 *   - Java 17+
 *   - JavaFX 17+
 *   - Maven
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationController controller = new SimulationController();
        VBox content = controller.createLayout();
        ScrollPane root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setFitToHeight(false);
        root.setPannable(true);
        root.setStyle("-fx-background: #f3f7fb; -fx-background-color: #f3f7fb;");

        Scene scene = new Scene(root, 1100, 820);

        primaryStage.setTitle("Producer-Consumer Simulation");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(680);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
