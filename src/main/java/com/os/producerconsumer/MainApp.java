package com.os.producerconsumer;

import com.os.producerconsumer.controller.SimulationController;
import javafx.application.Application;
import javafx.scene.Scene;
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
        VBox root = controller.createLayout();

        Scene scene = new Scene(root, 900, 800);

        primaryStage.setTitle("Producer-Consumer Simulation");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
