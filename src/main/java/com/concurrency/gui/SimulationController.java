package com.concurrency.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Controls the simulation of producers and consumers.
 * Manages threads and coordinates with the UI.
 */
public class SimulationController {

    private final BufferVisualizer visualizer;
    private final Consumer<String> logger;
    private final StatsUpdater statsUpdater;
    
    private final List<Thread> producerThreads = new ArrayList<>();
    private final List<Thread> consumerThreads = new ArrayList<>();
    private BlockingQueue<Integer> buffer;
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger totalProduced = new AtomicInteger(0);
    private final AtomicInteger totalConsumed = new AtomicInteger(0);
    
    private int baseDelay = 300;

    @FunctionalInterface
    public interface StatsUpdater {
        void update(int produced, int consumed, int inBuffer);
    }

    public SimulationController(BufferVisualizer visualizer, Consumer<String> logger, StatsUpdater statsUpdater) {
        this.visualizer = visualizer;
        this.logger = logger;
        this.statsUpdater = statsUpdater;
    }

    public void start(int numProducers, int numConsumers, int bufferSize, int delay) {
        if (running.get()) {
            return;
        }

        this.baseDelay = delay;
        running.set(true);
        totalProduced.set(0);
        totalConsumed.set(0);
        
        buffer = new ArrayBlockingQueue<>(bufferSize);
        
        producerThreads.clear();
        consumerThreads.clear();

        // Create producer threads
        for (int i = 1; i <= numProducers; i++) {
            final int id = i;
            Thread producer = new Thread(() -> runProducer(id), "Producer-" + i);
            producer.setDaemon(true);
            producerThreads.add(producer);
        }

        // Create consumer threads
        for (int i = 1; i <= numConsumers; i++) {
            final int id = i;
            Thread consumer = new Thread(() -> runConsumer(id), "Consumer-" + i);
            consumer.setDaemon(true);
            consumerThreads.add(consumer);
        }

        // Start all threads
        producerThreads.forEach(Thread::start);
        consumerThreads.forEach(Thread::start);
    }

    public void stop() {
        running.set(false);
        
        // Interrupt all threads
        producerThreads.forEach(Thread::interrupt);
        consumerThreads.forEach(Thread::interrupt);
        
        producerThreads.clear();
        consumerThreads.clear();
    }

    private void runProducer(int id) {
        String name = "Producer-" + id;
        String emoji = getProducerEmoji(id);
        int itemCount = 0;

        logger.accept(emoji + " " + name + " started");

        while (running.get()) {
            try {
                // Random delay before producing
                int delay = baseDelay + (int)(Math.random() * baseDelay);
                Thread.sleep(delay);

                if (!running.get()) break;

                itemCount++;
                int item = id * 1000 + itemCount;

                // Check if buffer is full
                if (buffer.remainingCapacity() == 0) {
                    logger.accept("⏳ " + name + " waiting (buffer full)");
                    visualizer.highlightWaiting(true);
                }

                // This will block if buffer is full
                buffer.put(item);
                
                int produced = totalProduced.incrementAndGet();
                visualizer.addItem(name);
                
                logger.accept(emoji + " " + name + " produced item #" + itemCount);
                updateStats();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.accept("🏁 " + name + " stopped (produced " + itemCount + " items)");
    }

    private void runConsumer(int id) {
        String name = "Consumer-" + id;
        String emoji = getConsumerEmoji(id);
        int itemCount = 0;

        logger.accept(emoji + " " + name + " started");

        while (running.get()) {
            try {
                // Random delay before consuming
                int delay = baseDelay + (int)(Math.random() * (baseDelay * 0.5));
                Thread.sleep(delay);

                if (!running.get()) break;

                // Check if buffer is empty
                if (buffer.isEmpty()) {
                    logger.accept("⏳ " + name + " waiting (buffer empty)");
                    visualizer.highlightWaiting(false);
                }

                // This will block if buffer is empty
                Integer item = buffer.poll();
                
                if (item != null) {
                    itemCount++;
                    int consumed = totalConsumed.incrementAndGet();
                    visualizer.removeItem(name);
                    
                    logger.accept(emoji + " " + name + " consumed item (value: " + item + ")");
                    updateStats();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.accept("🏁 " + name + " stopped (consumed " + itemCount + " items)");
    }

    private void updateStats() {
        statsUpdater.update(
            totalProduced.get(),
            totalConsumed.get(),
            buffer != null ? buffer.size() : 0
        );
    }

    private String getProducerEmoji(int id) {
        String[] emojis = {"🟢", "🟩", "💚", "✅", "🌿"};
        return emojis[(id - 1) % emojis.length];
    }

    private String getConsumerEmoji(int id) {
        String[] emojis = {"🔵", "🟦", "💙", "☑️", "🌊"};
        return emojis[(id - 1) % emojis.length];
    }
}

