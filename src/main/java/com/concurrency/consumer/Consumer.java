package com.concurrency.consumer;

import com.concurrency.buffer.Buffer;

/**
 * A Consumer thread that takes items from a shared buffer and processes them.
 *
 * <p>The consumer continuously attempts to take items from the buffer.
 * If the buffer is empty, the consumer will block until an item becomes
 * available.</p>
 *
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *   <li>Take items from the shared buffer safely</li>
 *   <li>Process the consumed items</li>
 *   <li>Handle interruption gracefully</li>
 * </ul>
 */
public class Consumer implements Runnable {

    private final Buffer<Integer> buffer;
    private final String name;
    private final int itemsToConsume;
    private final int consumptionDelayMs;
    private int totalConsumed = 0;

    /**
     * Creates a new Consumer.
     *
     * @param buffer             the shared buffer to take items from
     * @param name               identifier for this consumer (for logging)
     * @param itemsToConsume     number of items this consumer will process
     * @param consumptionDelayMs simulated delay for processing each item (in milliseconds)
     */
    public Consumer(Buffer<Integer> buffer, String name, int itemsToConsume, int consumptionDelayMs) {
        this.buffer = buffer;
        this.name = name;
        this.itemsToConsume = itemsToConsume;
        this.consumptionDelayMs = consumptionDelayMs;
    }

    /**
     * Creates a new Consumer with default delay of 150ms.
     *
     * @param buffer         the shared buffer to take items from
     * @param name           identifier for this consumer
     * @param itemsToConsume number of items to consume
     */
    public Consumer(Buffer<Integer> buffer, String name, int itemsToConsume) {
        this(buffer, name, itemsToConsume, 150);
    }

    @Override
    public void run() {
        System.out.printf("[%s] Starting consumption of %d items%n", name, itemsToConsume);

        try {
            for (int i = 1; i <= itemsToConsume; i++) {
                // Take an item from the buffer (may block if buffer is empty)
                System.out.printf("[%s] Waiting to consume item #%d...%n", name, i);
                Integer item = buffer.take();

                // Process the item
                processItem(item, i);
                totalConsumed++;

                // Simulate time taken to process the item
                if (consumptionDelayMs > 0 && i < itemsToConsume) {
                    Thread.sleep(consumptionDelayMs);
                }
            }
            System.out.printf("[%s] Finished consumption. Total items consumed: %d%n",
                    name, totalConsumed);

        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            System.out.printf("[%s] Consumption interrupted! Consumed %d items before interruption.%n",
                    name, totalConsumed);
        }
    }

    /**
     * Processes a consumed item.
     * In a real application, this could involve computation, storage, network calls, etc.
     *
     * @param item           the item to process
     * @param sequenceNumber the sequence number of this consumption
     */
    private void processItem(Integer item, int sequenceNumber) {
        // Simulate processing - in real scenarios this could be:
        // - Saving to database
        // - Sending over network
        // - Performing calculations
        // - Updating UI
        System.out.printf("[%s] Processing item #%d: %d (Item value squared: %d)%n",
                name, sequenceNumber, item, (long) item * item);
    }

    public String getName() {
        return name;
    }

    public int getTotalConsumed() {
        return totalConsumed;
    }
}

