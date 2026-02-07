package com.concurrency;

import com.concurrency.buffer.BlockingQueueBuffer;
import com.concurrency.buffer.Buffer;
import com.concurrency.buffer.SynchronizedBuffer;
import com.concurrency.consumer.Consumer;
import com.concurrency.producer.Producer;

import java.util.ArrayList;
import java.util.List;

/**
 * Main demonstration class for the Producer-Consumer problem.
 *
 * <p>This class demonstrates two different implementations:</p>
 * <ol>
 *   <li><b>SynchronizedBuffer</b>: Using synchronized/wait/notifyAll (classic approach)</li>
 *   <li><b>BlockingQueueBuffer</b>: Using java.util.concurrent.BlockingQueue (modern approach)</li>
 * </ol>
 *
 * <h2>The Producer-Consumer Problem</h2>
 * <p>The Producer-Consumer problem is a classic synchronization problem where:</p>
 * <ul>
 *   <li>One or more <b>producers</b> generate data and add it to a shared buffer</li>
 *   <li>One or more <b>consumers</b> remove data from the buffer and process it</li>
 *   <li>The buffer has a <b>fixed capacity</b> (bounded buffer)</li>
 * </ul>
 *
 * <h2>Synchronization Challenges:</h2>
 * <ul>
 *   <li><b>Mutual Exclusion</b>: Only one thread should modify the buffer at a time</li>
 *   <li><b>Buffer Full</b>: Producers must wait when buffer is full</li>
 *   <li><b>Buffer Empty</b>: Consumers must wait when buffer is empty</li>
 *   <li><b>No Deadlock</b>: System should not get stuck waiting forever</li>
 * </ul>
 */
public class ProducerConsumerDemo {

    private static final String SEPARATOR = "=".repeat(70);
    private static final String THIN_SEPARATOR = "-".repeat(70);

    public static void main(String[] args) {
        printHeader();

        System.out.println("\nChoose implementation to run:");
        System.out.println("1. Synchronized Buffer (wait/notifyAll)");
        System.out.println("2. BlockingQueue Buffer (java.util.concurrent)");
        System.out.println("3. Run both demonstrations");
        System.out.println("\nRunning default: Both demonstrations\n");

        try {
            // Demo 1: Classic synchronized approach
            runSynchronizedDemo();

            Thread.sleep(1000); // Brief pause between demos

            // Demo 2: Modern BlockingQueue approach
            runBlockingQueueDemo();

            printConclusion();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Demo interrupted!");
        }
    }

    /**
     * Demonstrates the Producer-Consumer problem using synchronized/wait/notifyAll.
     */
    private static void runSynchronizedDemo() throws InterruptedException {
        System.out.println(SEPARATOR);
        System.out.println("  DEMO 1: SYNCHRONIZED BUFFER (wait/notifyAll)");
        System.out.println(SEPARATOR);
        System.out.println("This uses synchronized methods, wait(), and notifyAll()");
        System.out.println("to coordinate between producers and consumers.\n");

        // Create a bounded buffer with capacity of 5
        Buffer<Integer> buffer = new SynchronizedBuffer<>(5);

        runDemo(buffer, 2, 2, 5);
    }

    /**
     * Demonstrates the Producer-Consumer problem using BlockingQueue.
     */
    private static void runBlockingQueueDemo() throws InterruptedException {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  DEMO 2: BLOCKING QUEUE BUFFER (java.util.concurrent)");
        System.out.println(SEPARATOR);
        System.out.println("This uses ArrayBlockingQueue which handles all synchronization");
        System.out.println("internally - a cleaner, more modern approach.\n");

        // Create a bounded buffer with capacity of 5
        Buffer<Integer> buffer = new BlockingQueueBuffer<>(5);

        runDemo(buffer, 2, 2, 5);
    }

    /**
     * Runs a producer-consumer demonstration with the given buffer.
     *
     * @param buffer           the buffer to use
     * @param numProducers     number of producer threads
     * @param numConsumers     number of consumer threads
     * @param itemsPerProducer number of items each producer will generate
     */
    private static void runDemo(Buffer<Integer> buffer, int numProducers,
                                 int numConsumers, int itemsPerProducer) throws InterruptedException {

        int totalItems = numProducers * itemsPerProducer;
        int itemsPerConsumer = totalItems / numConsumers;

        System.out.printf("Configuration:%n");
        System.out.printf("  - Buffer capacity: %d%n", buffer.capacity());
        System.out.printf("  - Producers: %d (each producing %d items)%n", numProducers, itemsPerProducer);
        System.out.printf("  - Consumers: %d (each consuming %d items)%n", numConsumers, itemsPerConsumer);
        System.out.printf("  - Total items to transfer: %d%n", totalItems);
        System.out.println(THIN_SEPARATOR);

        // Create producer threads
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= numProducers; i++) {
            Producer producer = new Producer(buffer, "Producer-" + i, itemsPerProducer, 50);
            Thread thread = new Thread(producer, "Producer-" + i);
            threads.add(thread);
        }

        // Create consumer threads
        for (int i = 1; i <= numConsumers; i++) {
            Consumer consumer = new Consumer(buffer, "Consumer-" + i, itemsPerConsumer, 75);
            Thread thread = new Thread(consumer, "Consumer-" + i);
            threads.add(thread);
        }

        // Start all threads
        System.out.println("\nStarting all threads...\n");
        long startTime = System.currentTimeMillis();

        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        long endTime = System.currentTimeMillis();

        System.out.println(THIN_SEPARATOR);
        System.out.printf("Demo completed in %d ms%n", endTime - startTime);
        System.out.printf("Final buffer state: %d items remaining%n", buffer.size());
    }

    private static void printHeader() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("        PRODUCER-CONSUMER PROBLEM DEMONSTRATION");
        System.out.println("            Java Concurrency Assignment");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("This program demonstrates the classic Producer-Consumer");
        System.out.println("synchronization problem using two different approaches:");
        System.out.println();
        System.out.println("1. Classic: synchronized/wait()/notifyAll()");
        System.out.println("2. Modern:  java.util.concurrent.BlockingQueue");
        System.out.println();
    }

    private static void printConclusion() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                    DEMONSTRATION COMPLETE");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("KEY TAKEAWAYS:");
        System.out.println();
        System.out.println("1. Both approaches solve the same problem correctly");
        System.out.println("2. synchronized/wait/notify shows the underlying concepts");
        System.out.println("3. BlockingQueue is preferred in production code:");
        System.out.println("   - Less error-prone");
        System.out.println("   - Well-tested and optimized");
        System.out.println("   - Cleaner code");
        System.out.println();
        System.out.println("CONCEPTS DEMONSTRATED:");
        System.out.println("- Thread synchronization");
        System.out.println("- Mutual exclusion");
        System.out.println("- Condition variables (wait/notify)");
        System.out.println("- Bounded buffer");
        System.out.println("- Thread coordination");
        System.out.println();
        System.out.println(SEPARATOR);
    }
}

