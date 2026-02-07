package com.concurrency.buffer;

/**
 * Interface defining the contract for a thread-safe buffer.
 * The buffer acts as the shared resource between producers and consumers.
 *
 * @param <T> the type of elements held in this buffer
 */
public interface Buffer<T> {

    /**
     * Adds an item to the buffer. If the buffer is full, the calling thread
     * will block until space becomes available.
     *
     * @param item the item to add to the buffer
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    void put(T item) throws InterruptedException;

    /**
     * Removes and returns an item from the buffer. If the buffer is empty,
     * the calling thread will block until an item becomes available.
     *
     * @return the item removed from the buffer
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    T take() throws InterruptedException;

    /**
     * Returns the current number of items in the buffer.
     *
     * @return the number of items currently in the buffer
     */
    int size();

    /**
     * Returns the maximum capacity of the buffer.
     *
     * @return the maximum number of items the buffer can hold
     */
    int capacity();

    /**
     * Checks if the buffer is empty.
     *
     * @return true if the buffer contains no items, false otherwise
     */
    boolean isEmpty();

    /**
     * Checks if the buffer is full.
     *
     * @return true if the buffer is at maximum capacity, false otherwise
     */
    boolean isFull();
}

