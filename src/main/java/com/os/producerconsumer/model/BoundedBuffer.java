package com.os.producerconsumer.model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BoundedBuffer implements the classic bounded buffer (producer-consumer) pattern
 * using Operating Systems synchronization primitives:
 *
 * - Semaphore emptySlots: counts empty slots in the buffer.
 * - Semaphore fullSlots:  counts filled slots in the buffer.
 * - ReentrantLock mutex:  ensures mutual exclusion when accessing the buffer.
 *
 * OS Concepts demonstrated:
 * - Semaphore: A signaling mechanism used to control access to a shared resource.
 * - Mutex: A locking mechanism that ensures only one thread accesses the critical section.
 * - Critical Section: The portion of code where the shared buffer is accessed.
 * - Race Condition Prevention: Semaphores and mutex coordinate producer/consumer threads.
 */
public class BoundedBuffer {

    private final Queue<BufferItem> buffer;
    private final int capacity;

    /*
     * Semaphore to track the number of empty slots in the buffer.
     * Producers acquire this before producing (decrementing empty count).
     * Consumers release this after consuming (incrementing empty count).
     */
    private final Semaphore emptySlots;

    /*
     * Semaphore to track the number of filled slots in the buffer.
     * Consumers acquire this before consuming (decrementing full count).
     * Producers release this after producing (incrementing full count).
     */
    private final Semaphore fullSlots;

    /*
     * ReentrantLock used as a mutex to protect the critical section
     * where the shared buffer queue is accessed.
     * Only one thread (producer or consumer) can hold this lock at a time.
     */
    private final ReentrantLock mutex;

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new LinkedList<>();
        // Start with all slots empty, none full
        this.emptySlots = new Semaphore(capacity);
        this.fullSlots = new Semaphore(0);
        this.mutex = new ReentrantLock();
    }

    /**
     * Producer calls this method to add an item to the buffer.
     *
     * Flow (demonstrates proper semaphore + mutex usage):
     * 1. Acquire emptySlots: wait until at least one slot is empty.
     * 2. Acquire mutex: enter critical section exclusively.
     * 3. Add item to the queue.
     * 4. Release mutex: exit critical section.
     * 5. Release fullSlots: signal consumers that an item is available.
     *
     * @param item       the item to produce
     * @param producerId the ID of the producer thread
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void produce(BufferItem item, int producerId) throws InterruptedException {
        // Wait for an empty slot (OS concept: semaphore wait/down operation)
        emptySlots.acquire();

        // Enter critical section (OS concept: mutex lock)
        mutex.lock();
        try {
            buffer.add(item);
        } finally {
            // Exit critical section (OS concept: mutex unlock)
            mutex.unlock();
        }

        // Signal that a slot is now full (OS concept: semaphore signal/up operation)
        fullSlots.release();
    }

    /**
     * Consumer calls this method to remove an item from the buffer.
     *
     * Flow (demonstrates proper semaphore + mutex usage):
     * 1. Acquire fullSlots: wait until at least one item is available.
     * 2. Acquire mutex: enter critical section exclusively.
     * 3. Remove item from the queue.
     * 4. Release mutex: exit critical section.
     * 5. Release emptySlots: signal producers that a slot is now empty.
     *
     * @param consumerId the ID of the consumer thread
     * @return the consumed item
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public BufferItem consume(int consumerId) throws InterruptedException {
        // Wait for a filled slot (OS concept: semaphore wait/down operation)
        fullSlots.acquire();

        BufferItem item;
        // Enter critical section (OS concept: mutex lock)
        mutex.lock();
        try {
            item = buffer.poll();
        } finally {
            // Exit critical section (OS concept: mutex unlock)
            mutex.unlock();
        }

        // Signal that a slot is now empty (OS concept: semaphore signal/up operation)
        emptySlots.release();

        return item;
    }

    /**
     * Takes a thread-safe snapshot of the current buffer contents as an array.
     * Uses mutex to ensure a consistent view while iterating.
     */
    public BufferItem[] snapshot() {
        mutex.lock();
        try {
            return buffer.toArray(new BufferItem[0]);
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Returns the current number of items in the buffer.
     */
    public int size() {
        mutex.lock();
        try {
            return buffer.size();
        } finally {
            mutex.unlock();
        }
    }

    public int getCapacity() {
        return capacity;
    }
}
