package com.concurrency.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Visual representation of the bounded buffer.
 * Shows slots that fill up as producers add items and empty as consumers take items.
 */
public class BufferVisualizer extends VBox {

    private final List<StackPane> slots = new ArrayList<>();
    private final List<Boolean> slotStates = new ArrayList<>();
    private HBox slotContainer;
    private int capacity;
    private int currentSize = 0;

    // Colors
    private static final Color EMPTY_COLOR = Color.web("#2d2d44");
    private static final Color FILLED_COLOR = Color.web("#E91E63");
    private static final Color PRODUCER_COLOR = Color.web("#4CAF50");
    private static final Color CONSUMER_COLOR = Color.web("#2196F3");
    private static final Color BORDER_COLOR = Color.web("#404060");

    public BufferVisualizer(int capacity) {
        this.capacity = capacity;
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPadding(new Insets(20));
        
        initializeSlots();
    }

    private void initializeSlots() {
        slotContainer = new HBox(8);
        slotContainer.setAlignment(Pos.CENTER);
        slotContainer.setPadding(new Insets(20));
        slotContainer.setStyle(
            "-fx-background-color: #0d0d1a;" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: #404060;" +
            "-fx-border-radius: 15;" +
            "-fx-border-width: 2;"
        );

        slots.clear();
        slotStates.clear();

        for (int i = 0; i < capacity; i++) {
            StackPane slot = createSlot(i);
            slots.add(slot);
            slotStates.add(false);
            slotContainer.getChildren().add(slot);
        }

        // Capacity label
        Label capacityLabel = new Label("Capacity: " + capacity);
        capacityLabel.setStyle("-fx-text-fill: #808080; -fx-font-size: 12px;");

        getChildren().clear();
        getChildren().addAll(slotContainer, capacityLabel);
    }

    private StackPane createSlot(int index) {
        StackPane slot = new StackPane();
        slot.setPrefSize(50, 50);
        slot.setMinSize(50, 50);
        slot.setMaxSize(50, 50);

        Rectangle bg = new Rectangle(46, 46);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(EMPTY_COLOR);
        bg.setStroke(BORDER_COLOR);
        bg.setStrokeWidth(2);

        Circle item = new Circle(18);
        item.setFill(FILLED_COLOR);
        item.setVisible(false);
        item.setEffect(new Glow(0.3));

        Label indexLabel = new Label(String.valueOf(index + 1));
        indexLabel.setStyle("-fx-text-fill: #505070; -fx-font-size: 10px;");
        StackPane.setAlignment(indexLabel, Pos.BOTTOM_CENTER);

        slot.getChildren().addAll(bg, item, indexLabel);
        return slot;
    }

    public void setCapacity(int newCapacity) {
        if (newCapacity != this.capacity) {
            this.capacity = newCapacity;
            this.currentSize = 0;
            Platform.runLater(this::initializeSlots);
        }
    }

    public void clear() {
        Platform.runLater(() -> {
            currentSize = 0;
            for (int i = 0; i < slots.size(); i++) {
                slotStates.set(i, false);
                Circle item = getItemCircle(i);
                if (item != null) {
                    item.setVisible(false);
                }
            }
        });
    }

    /**
     * Animates adding an item to the buffer.
     */
    public void addItem(String producerName) {
        Platform.runLater(() -> {
            // Find first empty slot
            int targetSlot = -1;
            for (int i = 0; i < slotStates.size(); i++) {
                if (!slotStates.get(i)) {
                    targetSlot = i;
                    break;
                }
            }

            if (targetSlot >= 0) {
                slotStates.set(targetSlot, true);
                currentSize++;
                animateAdd(targetSlot, producerName);
            }
        });
    }

    /**
     * Animates removing an item from the buffer.
     */
    public void removeItem(String consumerName) {
        Platform.runLater(() -> {
            // Find first filled slot (FIFO)
            int targetSlot = -1;
            for (int i = 0; i < slotStates.size(); i++) {
                if (slotStates.get(i)) {
                    targetSlot = i;
                    break;
                }
            }

            if (targetSlot >= 0) {
                slotStates.set(targetSlot, false);
                currentSize--;
                animateRemove(targetSlot, consumerName);
            }
        });
    }

    private void animateAdd(int slotIndex, String producerName) {
        StackPane slot = slots.get(slotIndex);
        Circle item = getItemCircle(slotIndex);
        Rectangle bg = getBackgroundRect(slotIndex);

        if (item != null && bg != null) {
            // Flash the slot green for producer
            Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(bg.strokeProperty(), PRODUCER_COLOR)),
                new KeyFrame(Duration.millis(300), 
                    new KeyValue(bg.strokeProperty(), BORDER_COLOR))
            );

            // Scale and fade in animation
            item.setScaleX(0);
            item.setScaleY(0);
            item.setOpacity(0);
            item.setVisible(true);
            item.setFill(FILLED_COLOR);

            ScaleTransition scale = new ScaleTransition(Duration.millis(200), item);
            scale.setToX(1);
            scale.setToY(1);
            scale.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(200), item);
            fade.setToValue(1);

            ParallelTransition add = new ParallelTransition(scale, fade, flash);
            add.play();

            // Add glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(PRODUCER_COLOR);
            glow.setRadius(15);
            slot.setEffect(glow);

            Timeline removeGlow = new Timeline(
                new KeyFrame(Duration.millis(400), e -> slot.setEffect(null))
            );
            removeGlow.play();
        }
    }

    private void animateRemove(int slotIndex, String consumerName) {
        StackPane slot = slots.get(slotIndex);
        Circle item = getItemCircle(slotIndex);
        Rectangle bg = getBackgroundRect(slotIndex);

        if (item != null && bg != null) {
            // Flash the slot blue for consumer
            Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(bg.strokeProperty(), CONSUMER_COLOR)),
                new KeyFrame(Duration.millis(300), 
                    new KeyValue(bg.strokeProperty(), BORDER_COLOR))
            );

            // Scale and fade out animation
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), item);
            scale.setToX(0);
            scale.setToY(0);
            scale.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fade = new FadeTransition(Duration.millis(200), item);
            fade.setToValue(0);

            ParallelTransition remove = new ParallelTransition(scale, fade, flash);
            remove.setOnFinished(e -> {
                item.setVisible(false);
                item.setScaleX(1);
                item.setScaleY(1);
                item.setOpacity(1);
            });
            remove.play();

            // Add glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(CONSUMER_COLOR);
            glow.setRadius(15);
            slot.setEffect(glow);

            Timeline removeGlow = new Timeline(
                new KeyFrame(Duration.millis(400), e -> slot.setEffect(null))
            );
            removeGlow.play();
        }
    }

    public void highlightWaiting(boolean isProducer) {
        Platform.runLater(() -> {
            Color waitColor = Color.web("#FF9800");
            DropShadow glow = new DropShadow();
            glow.setColor(waitColor);
            glow.setRadius(20);
            
            slotContainer.setEffect(glow);

            Timeline removeGlow = new Timeline(
                new KeyFrame(Duration.millis(500), e -> slotContainer.setEffect(null))
            );
            removeGlow.play();
        });
    }

    private Circle getItemCircle(int slotIndex) {
        if (slotIndex < slots.size()) {
            StackPane slot = slots.get(slotIndex);
            for (var node : slot.getChildren()) {
                if (node instanceof Circle) {
                    return (Circle) node;
                }
            }
        }
        return null;
    }

    private Rectangle getBackgroundRect(int slotIndex) {
        if (slotIndex < slots.size()) {
            StackPane slot = slots.get(slotIndex);
            for (var node : slot.getChildren()) {
                if (node instanceof Rectangle) {
                    return (Rectangle) node;
                }
            }
        }
        return null;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getCapacity() {
        return capacity;
    }
}

