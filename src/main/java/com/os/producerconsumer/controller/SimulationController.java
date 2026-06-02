package com.os.producerconsumer.controller;

import com.os.producerconsumer.model.BufferItem;
import com.os.producerconsumer.model.SimulationStats;
import com.os.producerconsumer.service.ConsumerWorker;
import com.os.producerconsumer.service.ProducerWorker;
import com.os.producerconsumer.service.SimulationService;
import com.os.producerconsumer.util.UiLogger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
    private FlowPane bufferDisplay;
    private static final int BUFFER_BOX_SIZE = 68;

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
        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setFillWidth(true);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f3f7fb, #e7eef6);");

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
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(4, 0, 10, 0));

        Label title = new Label("Producer-Consumer Simulation");
        title.setFont(Font.font("System", FontWeight.BOLD, 30));
        title.setStyle("-fx-text-fill: #16324f;");

        Label subtitle = new Label("Operating Systems Synchronization Problem");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 15));
        subtitle.setStyle("-fx-text-fill: #5f7285;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #4b7bec;");

        header.getChildren().addAll(title, subtitle, separator);
        return header;
    }

    /**
     * 2. Configuration Panel: Input controls and action buttons.
     */
    private VBox createConfigPanel() {
        VBox panel = new VBox(10);
        panel.setStyle(createCardStyle());
        panel.setPadding(new Insets(18));

        Label sectionTitle = new Label("Configuration");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #16324f;");

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
        inputGrid.setVgap(12);
        inputGrid.setPadding(new Insets(5, 0, 10, 0));
        inputGrid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(33.33);
            constraints.setHgrow(Priority.ALWAYS);
            inputGrid.getColumnConstraints().add(constraints);
        }

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
        VBox vb = new VBox(5);
        vb.setFillWidth(true);
        VBox.setVgrow(vb, Priority.NEVER);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        lbl.setStyle("-fx-text-fill: #555;");
        field.setMaxWidth(Double.MAX_VALUE);
        field.setPrefWidth(220);
        field.setStyle("-fx-padding: 8 10; -fx-border-color: #ccd6e0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-background-color: #fbfdff;");
        vb.getChildren().addAll(lbl, field);
        return vb;
    }

    /**
     * 3. Buffer Visualization Section: Shows buffer slots as colored boxes.
     */
    private VBox createBufferSection() {
        VBox section = new VBox(10);
        section.setStyle(createCardStyle());
        section.setPadding(new Insets(18));

        Label sectionTitle = new Label("Buffer Visualization");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #16324f;");

        Label helperText = new Label("Slots automatically wrap so every buffer item remains visible as the window size changes.");
        helperText.setStyle("-fx-text-fill: #5f7285; -fx-font-size: 11;");

        bufferDisplay = new FlowPane(Orientation.HORIZONTAL, 10, 10);
        bufferDisplay.setAlignment(Pos.CENTER_LEFT);
        bufferDisplay.setRowValignment(javafx.geometry.VPos.CENTER);
        bufferDisplay.setPrefWrapLength(900);
        bufferDisplay.setMinHeight(BUFFER_BOX_SIZE + 24);
        bufferDisplay.prefWrapLengthProperty().bind(section.widthProperty().subtract(36));

        ScrollPane scrollPane = new ScrollPane(bufferDisplay);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(170);
        scrollPane.setMinHeight(130);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        section.getChildren().addAll(sectionTitle, helperText, scrollPane);
        return section;
    }

    /**
     * 4 & 5. Producer and Consumer Status Sections (side by side).
     */
    private FlowPane createStatusSections() {
        FlowPane container = new FlowPane(15, 15);
        container.setPrefWrapLength(900);

        VBox producerSection = new VBox(10);
        producerSection.setStyle(createCardStyle());
        producerSection.setPadding(new Insets(18));
        producerSection.setPrefWidth(460);
        producerSection.setMinWidth(320);

        Label producerTitle = new Label("Producer Status");
        producerTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        producerTitle.setStyle("-fx-text-fill: #16324f;");

        producerStatusPanel = new VBox(5);
        producerStatusPanel.setPadding(new Insets(5, 0, 0, 0));
        VBox.setVgrow(producerStatusPanel, Priority.ALWAYS);

        producerSection.getChildren().addAll(producerTitle, producerStatusPanel);

        VBox consumerSection = new VBox(10);
        consumerSection.setStyle(createCardStyle());
        consumerSection.setPadding(new Insets(18));
        consumerSection.setPrefWidth(460);
        consumerSection.setMinWidth(320);

        Label consumerTitle = new Label("Consumer Status");
        consumerTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        consumerTitle.setStyle("-fx-text-fill: #16324f;");

        consumerStatusPanel = new VBox(5);
        consumerStatusPanel.setPadding(new Insets(5, 0, 0, 0));
        VBox.setVgrow(consumerStatusPanel, Priority.ALWAYS);

        consumerSection.getChildren().addAll(consumerTitle, consumerStatusPanel);

        container.getChildren().addAll(producerSection, consumerSection);
        return container;
    }

    /**
     * 6. Statistics Panel: Live counters showing simulation metrics.
     */
    private VBox createStatsPanel() {
        VBox section = new VBox(10);
        section.setStyle(createCardStyle());
        section.setPadding(new Insets(18));

        Label sectionTitle = new Label("Statistics");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #16324f;");

        FlowPane statsGrid = new FlowPane(Orientation.HORIZONTAL, 14, 14);
        statsGrid.setPrefWrapLength(900);
        statsGrid.setPadding(new Insets(5, 0, 0, 0));

        totalProducedLabel = createStatLabel("0");
        totalConsumedLabel = createStatLabel("0");
        currentBufferSizeLabel = createStatLabel("0");
        producerWaitLabel = createStatLabel("0");
        consumerWaitLabel = createStatLabel("0");
        simulationStatusLabel = createStatLabel("Stopped");

        statsGrid.getChildren().addAll(
                createStatEntry("Total Produced", totalProducedLabel),
                createStatEntry("Total Consumed", totalConsumedLabel),
                createStatEntry("Current Buffer Size", currentBufferSizeLabel),
                createStatEntry("Producer Wait Count", producerWaitLabel),
                createStatEntry("Consumer Wait Count", consumerWaitLabel),
                createStatEntry("Simulation Status", simulationStatusLabel)
        );

        section.getChildren().addAll(sectionTitle, statsGrid);
        return section;
    }

    private VBox createStatEntry(String name, Label value) {
        VBox card = new VBox(6);
        card.setPrefWidth(200);
        card.setMinWidth(180);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #f7faff; -fx-background-radius: 10; -fx-border-color: #d6e2ef; -fx-border-radius: 10;");

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        nameLabel.setStyle("-fx-text-fill: #5f7285;");
        value.setFont(Font.font("System", FontWeight.BOLD, 18));

        card.getChildren().addAll(nameLabel, value);
        return card;
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
        section.setStyle(createCardStyle());
        section.setPadding(new Insets(18));
        VBox.setVgrow(section, Priority.ALWAYS);

        Label sectionTitle = new Label("Event Logs");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #16324f;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(220);
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
            slot.setSpacing(2);

            if (i < snapshot.length && snapshot[i] != null) {
                int itemId = snapshot[i].getId();
                Label idLabel = new Label(String.valueOf(itemId));
                idLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                idLabel.setTextFill(Color.WHITE);

                Label slotLabel = new Label("[" + i + "]");
                slotLabel.setFont(Font.font("System", 9));
                slotLabel.setTextFill(Color.rgb(255, 255, 255, 0.7));

                slot.getChildren().addAll(idLabel, slotLabel);
                slot.setStyle("-fx-background-color: linear-gradient(to bottom, #4b7bec, #3867d6); -fx-border-color: #274baf; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
            } else {
                Label emptyLabel = new Label("empty");
                emptyLabel.setFont(Font.font("System", FontWeight.LIGHT, 11));
                emptyLabel.setTextFill(Color.rgb(150, 150, 150));

                Label slotLabel = new Label("[" + i + "]");
                slotLabel.setFont(Font.font("System", 9));
                slotLabel.setTextFill(Color.rgb(200, 200, 200));

                slot.getChildren().addAll(emptyLabel, slotLabel);
                slot.setStyle("-fx-background-color: #f8fbff; -fx-border-color: #bfd1e2; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-style: segments(5, 5);");
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
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: #f7faff; -fx-background-radius: 8; -fx-border-color: #d6e2ef; -fx-border-radius: 8;");

        // Status indicator circle
        Label indicator = new Label();
        indicator.setMinSize(12, 12);
        indicator.setMaxSize(12, 12);
        indicator.setStyle(getStatusStyle(status));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        nameLabel.setStyle("-fx-text-fill: #2c3e50;");
        nameLabel.setPrefWidth(110);

        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setStyle("-fx-text-fill: #555;");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

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

    private String createCardStyle() {
        return "-fx-background-color: rgba(255, 255, 255, 0.96); "
                + "-fx-border-color: #d6e2ef; "
                + "-fx-border-radius: 12; "
                + "-fx-background-radius: 12;";
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
