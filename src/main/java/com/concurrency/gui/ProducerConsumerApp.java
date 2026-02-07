package com.concurrency.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for visualizing the Producer-Consumer problem.
 * Provides a beautiful, animated GUI to demonstrate concurrency concepts.
 */
public class ProducerConsumerApp extends Application {

    private SimulationController controller;
    private BufferVisualizer bufferVisualizer;
    private TextArea logArea;
    private Button startButton;
    private Button stopButton;
    private Slider producerCountSlider;
    private Slider consumerCountSlider;
    private Slider bufferSizeSlider;
    private Slider speedSlider;
    private Label statsLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Producer-Consumer Problem Visualizer");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Center - Buffer Visualization
        bufferVisualizer = new BufferVisualizer(10);
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        
        Label bufferLabel = new Label("SHARED BUFFER");
        bufferLabel.getStyleClass().add("section-label");
        
        statsLabel = new Label("Produced: 0  |  Consumed: 0  |  In Buffer: 0");
        statsLabel.getStyleClass().add("stats-label");
        
        centerBox.getChildren().addAll(bufferLabel, bufferVisualizer, statsLabel);
        root.setCenter(centerBox);

        // Left - Controls
        VBox controls = createControls();
        root.setLeft(controls);

        // Right - Log
        VBox logPanel = createLogPanel();
        root.setRight(logPanel);

        // Initialize controller
        controller = new SimulationController(bufferVisualizer, this::log, this::updateStats);

        Scene scene = new Scene(root, 1200, 700);
        
        // Load CSS
        String css = getClass().getResource("/styles/app.css") != null 
            ? getClass().getResource("/styles/app.css").toExternalForm()
            : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }
        
        // Apply inline styles as fallback
        applyInlineStyles(root);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            controller.stop();
            Platform.exit();
        });
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 20, 10, 20));
        header.getStyleClass().add("header");

        Label title = new Label("PRODUCER-CONSUMER PROBLEM");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Java Concurrency Visualization");
        subtitle.setFont(Font.font("SF Pro Display", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#a0a0a0"));

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createControls() {
        VBox controls = new VBox(15);
        controls.setPadding(new Insets(20));
        controls.setPrefWidth(280);
        controls.getStyleClass().add("control-panel");

        Label controlTitle = new Label("CONTROLS");
        controlTitle.getStyleClass().add("section-label");

        // Producer count
        Label producerLabel = new Label("Producers: 2");
        producerCountSlider = new Slider(1, 5, 2);
        producerCountSlider.setShowTickLabels(true);
        producerCountSlider.setShowTickMarks(true);
        producerCountSlider.setMajorTickUnit(1);
        producerCountSlider.setMinorTickCount(0);
        producerCountSlider.setSnapToTicks(true);
        producerCountSlider.valueProperty().addListener((obs, old, val) -> 
            producerLabel.setText("Producers: " + val.intValue()));

        // Consumer count
        Label consumerLabel = new Label("Consumers: 2");
        consumerCountSlider = new Slider(1, 5, 2);
        consumerCountSlider.setShowTickLabels(true);
        consumerCountSlider.setShowTickMarks(true);
        consumerCountSlider.setMajorTickUnit(1);
        consumerCountSlider.setMinorTickCount(0);
        consumerCountSlider.setSnapToTicks(true);
        consumerCountSlider.valueProperty().addListener((obs, old, val) -> 
            consumerLabel.setText("Consumers: " + val.intValue()));

        // Buffer size
        Label bufferLabel = new Label("Buffer Size: 10");
        bufferSizeSlider = new Slider(3, 15, 10);
        bufferSizeSlider.setShowTickLabels(true);
        bufferSizeSlider.setShowTickMarks(true);
        bufferSizeSlider.setMajorTickUnit(3);
        bufferSizeSlider.setSnapToTicks(true);
        bufferSizeSlider.valueProperty().addListener((obs, old, val) -> {
            bufferLabel.setText("Buffer Size: " + val.intValue());
            bufferVisualizer.setCapacity(val.intValue());
        });

        // Speed
        Label speedLabel = new Label("Speed: Medium");
        speedSlider = new Slider(1, 5, 3);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setSnapToTicks(true);
        speedSlider.valueProperty().addListener((obs, old, val) -> {
            String[] speeds = {"", "Very Slow", "Slow", "Medium", "Fast", "Very Fast"};
            speedLabel.setText("Speed: " + speeds[val.intValue()]);
        });

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        startButton = new Button("▶ START");
        startButton.getStyleClass().add("start-button");
        startButton.setOnAction(e -> startSimulation());

        stopButton = new Button("■ STOP");
        stopButton.getStyleClass().add("stop-button");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopSimulation());

        buttonBox.getChildren().addAll(startButton, stopButton);

        // Legend
        VBox legend = createLegend();

        controls.getChildren().addAll(
            controlTitle,
            new Separator(),
            producerLabel, producerCountSlider,
            consumerLabel, consumerCountSlider,
            bufferLabel, bufferSizeSlider,
            speedLabel, speedSlider,
            buttonBox,
            new Separator(),
            legend
        );

        return controls;
    }

    private VBox createLegend() {
        VBox legend = new VBox(8);
        legend.setPadding(new Insets(10, 0, 0, 0));

        Label legendTitle = new Label("LEGEND");
        legendTitle.getStyleClass().add("legend-title");

        HBox producer = createLegendItem("●", "#4CAF50", "Producer adding");
        HBox consumer = createLegendItem("●", "#2196F3", "Consumer taking");
        HBox waiting = createLegendItem("●", "#FF9800", "Thread waiting");
        HBox empty = createLegendItem("○", "#555555", "Empty slot");
        HBox full = createLegendItem("●", "#E91E63", "Filled slot");

        legend.getChildren().addAll(legendTitle, producer, consumer, waiting, empty, full);
        return legend;
    }

    private HBox createLegendItem(String symbol, String color, String text) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label dot = new Label(symbol);
        dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px;");
        
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
        
        item.getChildren().addAll(dot, label);
        return item;
    }

    private VBox createLogPanel() {
        VBox logPanel = new VBox(10);
        logPanel.setPadding(new Insets(20));
        logPanel.setPrefWidth(350);
        logPanel.getStyleClass().add("log-panel");

        Label logTitle = new Label("ACTIVITY LOG");
        logTitle.getStyleClass().add("section-label");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(25);
        logArea.getStyleClass().add("log-area");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        Button clearButton = new Button("Clear Log");
        clearButton.getStyleClass().add("clear-button");
        clearButton.setOnAction(e -> logArea.clear());

        logPanel.getChildren().addAll(logTitle, logArea, clearButton);
        return logPanel;
    }

    private void startSimulation() {
        int producers = (int) producerCountSlider.getValue();
        int consumers = (int) consumerCountSlider.getValue();
        int bufferSize = (int) bufferSizeSlider.getValue();
        int speed = (int) speedSlider.getValue();

        // Calculate delay based on speed (inverse relationship)
        int baseDelay = 600 - (speed * 100); // 500ms at speed 1, 100ms at speed 5

        bufferVisualizer.setCapacity(bufferSize);
        bufferVisualizer.clear();
        logArea.clear();

        startButton.setDisable(true);
        stopButton.setDisable(false);
        disableSliders(true);

        log("🚀 Starting simulation...");
        log("   Producers: " + producers + ", Consumers: " + consumers);
        log("   Buffer size: " + bufferSize);
        log("");

        controller.start(producers, consumers, bufferSize, baseDelay);
    }

    private void stopSimulation() {
        controller.stop();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        disableSliders(false);
        log("");
        log("🛑 Simulation stopped.");
    }

    private void disableSliders(boolean disable) {
        producerCountSlider.setDisable(disable);
        consumerCountSlider.setDisable(disable);
        bufferSizeSlider.setDisable(disable);
    }

    private void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void updateStats(int produced, int consumed, int inBuffer) {
        Platform.runLater(() -> {
            statsLabel.setText(String.format(
                "Produced: %d  |  Consumed: %d  |  In Buffer: %d",
                produced, consumed, inBuffer
            ));
        });
    }

    private void applyInlineStyles(BorderPane root) {
        root.setStyle("-fx-background-color: #1a1a2e;");
        
        // Style header
        if (root.getTop() != null) {
            root.getTop().setStyle("-fx-background-color: linear-gradient(to right, #16213e, #1a1a2e); -fx-padding: 20;");
        }
        
        // Style control panel
        if (root.getLeft() != null) {
            root.getLeft().setStyle("-fx-background-color: #16213e; -fx-padding: 20;");
        }
        
        // Style log panel
        if (root.getRight() != null) {
            root.getRight().setStyle("-fx-background-color: #16213e; -fx-padding: 20;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

