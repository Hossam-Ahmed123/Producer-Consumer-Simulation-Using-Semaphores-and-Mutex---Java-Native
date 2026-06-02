package com.os.producerconsumer.controller;

import com.os.producerconsumer.model.BufferItem;
import com.os.producerconsumer.model.SimulationStats;
import com.os.producerconsumer.service.ConsumerWorker;
import com.os.producerconsumer.service.ProducerWorker;
import com.os.producerconsumer.service.SimulationService;
import com.os.producerconsumer.util.UiLogger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Main UI Controller for the Producer-Consumer Simulation.
 * Builds the JavaFX interface and wires it to the SimulationService.
 *
 * The UI is organized into clearly labeled sections for easy
 * demonstration during university presentations.
 */
public class SimulationController {

    private final SimulationService simulationService;
    private UiLogger uiLogger;

    // Configuration controls
    private TextField bufferSizeField;
    private TextField producerCountField;
    private TextField consumerCountField;
    private TextField totalItemsField;
    private TextField producerSpeedField;
    private TextField consumerSpeedField;

    // Buttons
    private Button startButton;
    private Button stopButton;
    private Button resetButton;

    // Buffer visualization
    private HBox bufferDisplay;
    private final int BUFFER_BOX_SIZE = 60;

    // Status labels
    private VBox producerStatusPanel;
    private VBox consumerStatusPanel;

    // Statistics labels
    private Label totalProducedLabel;
    private Label totalConsumedLabel;
    private Label currentBufferSizeLabel;
    private Label producerWaitLabel;
    private Label consumerWaitLabel;
    private Label simulationStatusLabel;

    // Log area
    private TextArea logArea;

    // Periodic UI update timer
    private volatile boolean updateScheduled = false;

    public SimulationController() {
        this.simulationService = new SimulationService();
    }

    /**
     * Builds the complete UI layout.
     */
    public VBox createLayout() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f4f4;");

        root.getChildren().addAll(
                createHeader(),
                createConfigPanel(),
                createBufferSection(),
                createStatusSections(),
                createStatsPanel(),
                createLogSection()
        );

        return root;
    }

    /**
     * 1. Header Section: Project title and subtitle.
     */
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 15, 0));

        Label title = new Label("Producer-Consumer Simulation");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Operating Systems Synchronization Problem");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setStyle("-fx-text-fill: #7f8c8d;");

        // Decorative line
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #3498db;");

        header.getChildren().addAll(title, subtitle, separator);
        return header;
    }

    /**
     * 2. Configuration Panel: Input controls and action buttons.
     */
    private VBox createConfigPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        panel.setPadding(new Insets(15));

        Label sectionTitle = new Label("Configuration");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #2c3e50;");

        // Input grid
        GridField[] fields = new GridField[]{
                new GridField("Buffer Size:", "5"),
                new GridField("Number of Producers:", "2"),
                new GridField("Number of Consumers:", "2"),
                new GridField("Total Items to Produce:", "30"),
                new GridField("Producer Speed (ms):", "700"),
                new GridField("Consumer Speed (ms):", "1000")
        };

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(15);
        inputGrid.setVgap(8);
        inputGrid.setPadding(new Insets(5, 0, 10, 0));

        // Column constraints for even spacing
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        inputGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        bufferSizeField = new TextField(fields[0].defaultValue);
        producerCountField = new TextField(fields[1].defaultValue);
        consumerCountField = new TextField(fields[2].defaultValue);
        totalItemsField = new TextField(fields[3].defaultValue);
        producerSpeedField = new TextField(fields[4].defaultValue);
        consumerSpeedField = new TextField(fields[5].defaultValue);

        // Place in two rows of three
        inputGrid.add(createLabeledField(fields[0].label, bufferSizeField), 0, 0);
        inputGrid.add(createLabeledField(fields[1].label, producerCountField), 1, 0);
        inputGrid.add(createLabeledField(fields[2].label, consumerCountField), 2, 0);
        inputGrid.add(createLabeledField(fields[3].label, totalItemsField), 0, 1);
        inputGrid.add(createLabeledField(fields[4].label, producerSpeedField), 1, 1);
        inputGrid.add(createLabeledField(fields[5].label, consumerSpeedField), 2, 1);

        // Buttons
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        startButton = new Button("Start Simulation");
        startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        startButton.setOnAction(e -> startSimulation());

        stopButton = new Button("Stop Simulation");
        stopButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> simulationService.stop());

        resetButton = new Button("Reset Simulation");
        resetButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        resetButton.setOnAction(e -> {
            simulationService.reset();
            updateUI();
        });

        buttonBar.getChildren().addAll(startButton, stopButton, resetButton);

        panel.getChildren().addAll(sectionTitle, inputGrid, buttonBar);
        return panel;
    }

    private VBox createLabeledField(String labelText, TextField field) {
        VBox vb = new VBox(3);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        lbl.setStyle("-fx-text-fill: #555;");
        field.setMaxWidth(140);
        field.setStyle("-fx-padding: 6; -fx-border-color: #ccc; -fx-border-radius: 3;");
        vb.getChildren().addAll(lbl, field);
        return vb;
    }

    /**
     * 3. Buffer Visualization Section: Shows buffer slots as colored boxes.
     */
    private VBox createBufferSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setPadding(new Insets(15));

        Label sectionTitle = new Label("Buffer Visualization");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #2c3e50;");

        bufferDisplay = new HBox(8);
        bufferDisplay.setAlignment(Pos.CENTER_LEFT);
        bufferDisplay.setMinHeight(BUFFER_BOX_SIZE + 20);

        // Scroll pane for buffer if it gets large
        ScrollPane scrollPane = new ScrollPane(bufferDisplay);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(100);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        section.getChildren().addAll(sectionTitle, scrollPane);
        return section;
    }

    /**
     * 4 & 5. Producer and Consumer Status Sections (side by side).
     */
    private HBox createStatusSections() {
        HBox container = new HBox(15);

        // Producer Status Panel
        VBox producerSection = new VBox(10);
        producerSection.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        producerSection.setPadding(new Insets(15));
        producerSection.setPrefWidth(400);

        Label producerTitle = new Label("Producer Status");
        producerTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        producerTitle.setStyle("-fx-text-fill: #2c3e50;");

        producerStatusPanel = new VBox(5);
        producerStatusPanel.setPadding(new Insets(5, 0, 0, 0));

        producerSection.getChildren().addAll(producerTitle, producerStatusPanel);

        // Consumer Status Panel
        VBox consumerSection = new VBox(10);
        consumerSection.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        consumerSection.setPadding(new Insets(15));
        consumerSection.setPrefWidth(400);

        Label consumerTitle = new Label("Consumer Status");
        consumerTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        consumerTitle.setStyle("-fx-text-fill: #2c3e50;");

        consumerStatusPanel = new VBox(5);
        consumerStatusPanel.setPadding(new Insets(5, 0, 0, 0));

        consumerSection.getChildren().addAll(consumerTitle, consumerStatusPanel);

        container.getChildren().addAll(producerSection, consumerSection);
        HBox.setHgrow(producerSection, Priority.ALWAYS);
        HBox.setHgrow(consumerSection, Priority.ALWAYS);

        return container;
    }

    /**
     * 6. Statistics Panel: Live counters showing simulation metrics.
     */
    private VBox createStatsPanel() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setPadding(new Insets(15));

        Label sectionTitle = new Label("Statistics");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #2c3e50;");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(8);
        statsGrid.setPadding(new Insets(5, 0, 0, 0));

        totalProducedLabel = createStatLabel("0");
        totalConsumedLabel = createStatLabel("0");
        currentBufferSizeLabel = createStatLabel("0");
        producerWaitLabel = createStatLabel("0");
        consumerWaitLabel = createStatLabel("0");
        simulationStatusLabel = createStatLabel("Stopped");

        statsGrid.add(createStatEntry("Total Produced:", totalProducedLabel), 0, 0);
        statsGrid.add(createStatEntry("Total Consumed:", totalConsumedLabel), 1, 0);
        statsGrid.add(createStatEntry("Current Buffer Size:", currentBufferSizeLabel), 2, 0);

        statsGrid.add(createStatEntry("Producer Wait Count:", producerWaitLabel), 0, 1);
        statsGrid.add(createStatEntry("Consumer Wait Count:", consumerWaitLabel), 1, 1);
        statsGrid.add(createStatEntry("Simulation Status:", simulationStatusLabel), 2, 1);

        section.getChildren().addAll(sectionTitle, statsGrid);
        return section;
    }

    private HBox createStatEntry(String name, Label value) {
        HBox hb = new HBox(5);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        nameLabel.setStyle("-fx-text-fill: #555;");
        value.setFont(Font.font("System", FontWeight.BOLD, 12));
        hb.getChildren().addAll(nameLabel, value);
        return hb;
    }

    private Label createStatLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #2c3e50;");
        return label;
    }

    /**
     * 7. Logs Panel: Timestamped event log.
     */
    private VBox createLogSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setPadding(new Insets(15));
        VBox.setVgrow(section, Priority.ALWAYS);

        Label sectionTitle = new Label("Event Logs");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #2c3e50;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 11; -fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4;");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        uiLogger = new UiLogger(logArea);

        section.getChildren().addAll(sectionTitle, logArea);
        return section;
    }

    /**
     * Starts the simulation after validating user input.
     */
    private void startSimulation() {
        try {
            int bufferSize = Integer.parseInt(bufferSizeField.getText().trim());
            int producerCount = Integer.parseInt(producerCountField.getText().trim());
            int consumerCount = Integer.parseInt(consumerCountField.getText().trim());
            int totalItems = Integer.parseInt(totalItemsField.getText().trim());
            int producerSpeed = Integer.parseInt(producerSpeedField.getText().trim());
            int consumerSpeed = Integer.parseInt(consumerSpeedField.getText().trim());

            // Validate all inputs are positive
            if (bufferSize <= 0 || producerCount <= 0 || consumerCount <= 0
                    || totalItems <= 0 || producerSpeed <= 0 || consumerSpeed <= 0) {
                showAlert("All values must be greater than 0.");
                return;
            }

            startButton.setDisable(true);
            stopButton.setDisable(false);

            simulationService.configure(bufferSize, producerCount, consumerCount,
                    totalItems, producerSpeed, consumerSpeed, uiLogger, this::updateUI);
            simulationService.start();

        } catch (NumberFormatException e) {
            showAlert("Please enter valid numeric values in all fields.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Updates all UI components with the latest simulation state.
     * Called via Platform.runLater() from any thread.
     */
    public void updateUI() {
        if (updateScheduled) return;
        updateScheduled = true;

        Platform.runLater(() -> {
            try {
                boolean running = simulationService.isRunning();

                // Update button states
                startButton.setDisable(running);
                stopButton.setDisable(!running);

                // Disable inputs while running
                boolean disableInputs = running;
                bufferSizeField.setDisable(disableInputs);
                producerCountField.setDisable(disableInputs);
                consumerCountField.setDisable(disableInputs);
                totalItemsField.setDisable(disableInputs);
                producerSpeedField.setDisable(disableInputs);
                consumerSpeedField.setDisable(disableInputs);

                // Update buffer visualization
                updateBufferDisplay();

                // Update producer/consumer statuses
                updateStatusPanels();

                // Update statistics
                updateStats();
            } finally {
                updateScheduled = false;
            }
        });
    }

    /**
     * Renders the buffer as a row of styled boxes.
     * Filled slots show the item ID; empty slots show "empty".
     */
    private void updateBufferDisplay() {
        bufferDisplay.getChildren().clear();

        BufferItem[] snapshot = simulationService.getBufferSnapshot();
        int capacity = simulationService.getBufferCapacity();

        for (int i = 0; i < capacity; i++) {
            VBox slot = new VBox();
            slot.setAlignment(Pos.CENTER);
            slot.setPrefSize(BUFFER_BOX_SIZE, BUFFER_BOX_SIZE);
            slot.setMinSize(BUFFER_BOX_SIZE, BUFFER_BOX_SIZE);

            if (i < snapshot.length && snapshot[i] != null) {
                // Filled slot
                int itemId = snapshot[i].getId();
                Label idLabel = new Label(String.valueOf(itemId));
                idLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                idLabel.setTextFill(Color.WHITE);

                Label slotLabel = new Label("[" + i + "]");
                slotLabel.setFont(Font.font("System", 9));
                slotLabel.setTextFill(Color.rgb(255, 255, 255, 0.7));

                slot.getChildren().addAll(idLabel, slotLabel);
                slot.setStyle("-fx-background-color: #3498db; -fx-border-color: #2980b9; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-radius: 4;");
            } else {
                // Empty slot
                Label emptyLabel = new Label("empty");
                emptyLabel.setFont(Font.font("System", FontWeight.LIGHT, 11));
                emptyLabel.setTextFill(Color.rgb(150, 150, 150));

                Label slotLabel = new Label("[" + i + "]");
                slotLabel.setFont(Font.font("System", 9));
                slotLabel.setTextFill(Color.rgb(200, 200, 200));

                slot.getChildren().addAll(emptyLabel, slotLabel);
                slot.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-radius: 4; -fx-border-style: dashed;");
            }

            bufferDisplay.getChildren().add(slot);
        }
    }

    /**
     * Updates the producer and consumer status labels.
     */
    private void updateStatusPanels() {
        // Update producers
        producerStatusPanel.getChildren().clear();
        List<ProducerWorker> producers = simulationService.getProducers();
        if (producers.isEmpty()) {
            producerStatusPanel.getChildren().add(new Label("No producers configured"));
        } else {
            for (ProducerWorker p : producers) {
                producerStatusPanel.getChildren().add(createStatusRow(
                        "Producer " + p.getName().replace("Producer-", ""),
                        p.getStatus()));
            }
        }

        // Update consumers
        consumerStatusPanel.getChildren().clear();
        List<ConsumerWorker> consumers = simulationService.getConsumers();
        if (consumers.isEmpty()) {
            consumerStatusPanel.getChildren().add(new Label("No consumers configured"));
        } else {
            for (ConsumerWorker c : consumers) {
                consumerStatusPanel.getChildren().add(createStatusRow(
                        "Consumer " + c.getName().replace("Consumer-", ""),
                        c.getStatus()));
            }
        }
    }

    /**
     * Creates a single status row with colored indicator.
     */
    private HBox createStatusRow(String name, String status) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(3, 0, 3, 0));

        // Status indicator circle
        Label indicator = new Label();
        indicator.setMinSize(12, 12);
        indicator.setMaxSize(12, 12);
        indicator.setStyle(getStatusStyle(status));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        nameLabel.setStyle("-fx-text-fill: #2c3e50;");
        nameLabel.setPrefWidth(100);

        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setStyle("-fx-text-fill: #555;");

        row.getChildren().addAll(indicator, nameLabel, statusLabel);
        return row;
    }

    /**
     * Returns a CSS style string for the status indicator dot.
     */
    private String getStatusStyle(String status) {
        if (status == null) {
            return "-fx-background-color: #95a5a6; -fx-background-radius: 50%;";
        }
        switch (status) {
            case "Running":
                return "-fx-background-color: #27ae60; -fx-background-radius: 50%;";
            case "Waiting (buffer full)":
            case "Waiting (buffer empty)":
                return "-fx-background-color: #f39c12; -fx-background-radius: 50%;";
            case "Stopped":
                return "-fx-background-color: #e74c3c; -fx-background-radius: 50%;";
            default:
                return "-fx-background-color: #95a5a6; -fx-background-radius: 50%;";
        }
    }

    /**
     * Updates the statistics panel with live counter values.
     */
    private void updateStats() {
        SimulationStats stats = simulationService.getStats();
        if (stats == null) return;

        totalProducedLabel.setText(String.valueOf(stats.getTotalProduced()));
        totalConsumedLabel.setText(String.valueOf(stats.getTotalConsumed()));
        currentBufferSizeLabel.setText(String.valueOf(simulationService.getCurrentBufferSize()));
        producerWaitLabel.setText(String.valueOf(stats.getProducerWaitCount()));
        consumerWaitLabel.setText(String.valueOf(stats.getConsumerWaitCount()));

        String simStatus = stats.getStatus();
        simulationStatusLabel.setText(simStatus);

        // Color the status label based on state
        if ("Running".equals(simStatus)) {
            simulationStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else if ("Completed".equals(simStatus)) {
            simulationStatusLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
        } else {
            simulationStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    /**
     * Internal helper for configuration field definitions.
     */
    private static class GridField {
        final String label;
        final String defaultValue;

        GridField(String label, String defaultValue) {
            this.label = label;
            this.defaultValue = defaultValue;
        }
    }
}
