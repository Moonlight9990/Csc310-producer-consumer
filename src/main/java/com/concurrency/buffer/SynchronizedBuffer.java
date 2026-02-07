package com.concurrency.buffer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A thread-safe bounded buffer implementation using intrinsic locks (synchronized).
 * This implementation uses wait() and notifyAll() for thread coordination.
 *
 * <p>This is the classic approach to solving the Producer-Consumer problem,
 * demonstrating fundamental Java concurrency concepts.</p>
 *
 * <h2>How it works:</h2>
 * <ul>
 *   <li>Producers call put() to add items - they wait if buffer is full</li>
 *   <li>Consumers call take() to remove items - they wait if buffer is empty</li>
 *   <li>synchronized keyword ensures mutual exclusion (only one thread at a time)</li>
 *   <li>wait() releases the lock and suspends the thread</li>
 *   <li>notifyAll() wakes up all waiting threads to re-check conditions</li>
 * </ul>
 *
 * @param <T> the type of elements held in this buffer
 */
public class SynchronizedBuffer<T> implements Buffer<T> {

    private final Queue<T> queue;
    private final int capacity;

    /**
     * Creates a new SynchronizedBuffer with the specified capacity.
     *
     * @param capacity the maximum number of items the buffer can hold
     * @throws IllegalArgumentException if capacity is less than 1
     */
    public SynchronizedBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Buffer capacity must be at least 1");
        }
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    /**
     * Adds an item to the buffer. Blocks if the buffer is full until space is available.
     *
     * <p>Thread-safety mechanism:</p>
     * <ol>
     *   <li>Acquire intrinsic lock via synchronized</li>
     *   <li>While buffer is full, release lock and wait</li>
     *   <li>Once space available, add item to queue</li>
     *   <li>Notify all waiting threads (consumers can now take)</li>
     *   <li>Release lock on method exit</li>
     * </ol>
     *
     * @param item the item to add
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public synchronized void put(T item) throws InterruptedException {
        // Wait while buffer is full - use while loop to guard against spurious wakeups
        while (queue.size() == capacity) {
            System.out.printf("[BUFFER] Full (size=%d). Producer waiting...%n", queue.size());
            wait(); // Release lock and wait until notified
        }

        queue.add(item);
        System.out.printf("[BUFFER] Item added. New size: %d/%d%n", queue.size(), capacity);

        // Wake up all waiting threads (consumers waiting for items)
        notifyAll();
    }

    /**
     * Removes and returns an item from the buffer. Blocks if the buffer is empty.
     *
     * <p>Thread-safety mechanism:</p>
     * <ol>
     *   <li>Acquire intrinsic lock via synchronized</li>
     *   <li>While buffer is empty, release lock and wait</li>
     *   <li>Once item available, remove from queue</li>
     *   <li>Notify all waiting threads (producers can now put)</li>
     *   <li>Release lock on method exit</li>
     * </ol>
     *
     * @return the removed item
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public synchronized T take() throws InterruptedException {
        // Wait while buffer is empty - use while loop to guard against spurious wakeups
        while (queue.isEmpty()) {
            System.out.printf("[BUFFER] Empty (size=%d). Consumer waiting...%n", queue.size());
            wait(); // Release lock and wait until notified
        }

        T item = queue.remove();
        System.out.printf("[BUFFER] Item removed. New size: %d/%d%n", queue.size(), capacity);

        // Wake up all waiting threads (producers waiting for space)
        notifyAll();

        return item;
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public synchronized boolean isFull() {
        return queue.size() == capacity;
    }
}

