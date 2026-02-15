package com.concurrency.producer;

import com.concurrency.buffer.Buffer;

/**
 * A Producer thread that generates items and places them into a shared buffer.
 *
 * <p>The producer continuously generates items (in this case, sequential integers)
 * and attempts to put them into the buffer. If the buffer is full, the producer
 * will block until space becomes available.</p>
 *
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *   <li>Generate data/items to be processed</li>
 *   <li>Place items into the shared buffer safely</li>
 *   <li>Handle interruption gracefully</li>
 * </ul>
 */
public class Producer implements Runnable {

    private final Buffer<Integer> buffer;
    private final String name;
    private final int itemsToProduce;
    private final int productionDelayMs;

    /**
     * Creates a new Producer.
     *
     * @param buffer            the shared buffer to put items into
     * @param name              identifier for this producer (for logging)
     * @param itemsToProduce    number of items this producer will generate
     * @param productionDelayMs simulated delay between productions (in milliseconds)
     */
    public Producer(Buffer<Integer> buffer, String name, int itemsToProduce, int productionDelayMs) {
        this.buffer = buffer;
        this.name = name;
        this.itemsToProduce = itemsToProduce;
        this.productionDelayMs = productionDelayMs;
    }

    /**
     * Creates a new Producer with default delay of 100ms.
     *
     * @param buffer         the shared buffer to put items into
     * @param name           identifier for this producer
     * @param itemsToProduce number of items to produce
     */
    public Producer(Buffer<Integer> buffer, String name, int itemsToProduce) {
        this(buffer, name, itemsToProduce, 100);
    }

    @Override
    public void run() {
        System.out.printf("[%s] Starting production of %d items%n", name, itemsToProduce);

        try {
            for (int i = 1; i <= itemsToProduce; i++) {
                // Generate the item (could be any complex computation in real scenarios)
                int item = generateItem(i);

                // Put the item into the buffer (may block if buffer is full)
                System.out.printf("[%s] Producing item #%d: %d%n", name, i, item);
                buffer.put(item);
                System.out.printf("[%s] Successfully produced item #%d%n", name, i);

                // Simulate time taken to produce next item
                if (productionDelayMs > 0 && i < itemsToProduce) {
                    Thread.sleep(productionDelayMs);
                }
            }
            System.out.printf("[%s] Finished production. Total items produced: %d%n",
                    name, itemsToProduce);

        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            System.out.printf("[%s] Production interrupted!%n", name);
        }
    }

    /**
     * Generates an item to be produced.
     * In this simple implementation, we generate sequential numbers multiplied
     * by a hash of the producer name to differentiate between producers.
     *
     * @param sequenceNumber the sequence number of the item
     * @return the generated item
     */
    private int generateItem(int sequenceNumber) {
        // Create unique items by combining producer identity with sequence number
        return Math.abs(name.hashCode() % 100) * 1000 + sequenceNumber;
    }

    public String getName() {
        return name;
    }
}

