package com.concurrency.buffer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A thread-safe bounded buffer implementation using Java's BlockingQueue.
 * This is the modern, recommended approach for Producer-Consumer problems.
 *
 * <p>BlockingQueue handles all synchronization internally, making the code
 * simpler and less error-prone. It uses efficient lock-based mechanisms
 * under the hood.</p>
 *
 * <h2>Why use BlockingQueue?</h2>
 * <ul>
 *   <li>Thread-safe by design - no explicit synchronization needed</li>
 *   <li>Well-tested and optimized by Java experts</li>
 *   <li>Reduces chances of bugs (deadlocks, race conditions)</li>
 *   <li>Cleaner, more readable code</li>
 * </ul>
 *
 * @param <T> the type of elements held in this buffer
 */
public class BlockingQueueBuffer<T> implements Buffer<T> {

    private final BlockingQueue<T> queue;
    private final int capacity;

    /**
     * Creates a new BlockingQueueBuffer with the specified capacity.
     *
     * @param capacity the maximum number of items the buffer can hold
     * @throws IllegalArgumentException if capacity is less than 1
     */
    public BlockingQueueBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Buffer capacity must be at least 1");
        }
        this.capacity = capacity;
        // ArrayBlockingQueue is a bounded, blocking queue backed by an array
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Adds an item to the buffer using BlockingQueue's put method.
     * Automatically blocks if the queue is full.
     *
     * @param item the item to add
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public void put(T item) throws InterruptedException {
        System.out.printf("[BUFFER] Attempting to add item. Current size: %d/%d%n",
                queue.size(), capacity);
        queue.put(item); // Blocks if full
        System.out.printf("[BUFFER] Item added. New size: %d/%d%n", queue.size(), capacity);
    }

    /**
     * Removes and returns an item using BlockingQueue's take method.
     * Automatically blocks if the queue is empty.
     *
     * @return the removed item
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public T take() throws InterruptedException {
        System.out.printf("[BUFFER] Attempting to take item. Current size: %d/%d%n",
                queue.size(), capacity);
        T item = queue.take(); // Blocks if empty
        System.out.printf("[BUFFER] Item removed. New size: %d/%d%n", queue.size(), capacity);
        return item;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean isFull() {
        return queue.size() == capacity;
    }
}

