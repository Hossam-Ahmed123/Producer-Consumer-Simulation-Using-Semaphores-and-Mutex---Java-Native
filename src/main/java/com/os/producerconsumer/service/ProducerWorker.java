package com.os.producerconsumer.service;

import com.os.producerconsumer.model.BoundedBuffer;
import com.os.producerconsumer.model.BufferItem;
import com.os.producerconsumer.model.SimulationStats;
import com.os.producerconsumer.util.UiLogger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a Producer thread in the simulation.
 *
 * OS Concept: Each Producer runs in its own thread and generates items
 * into the shared bounded buffer. Producers must wait when the buffer is full
 * (demonstrating semaphore-based synchronization).
 */
public class ProducerWorker extends Thread {

    private final int producerId;
    private final BoundedBuffer buffer;
    private final SimulationStats stats;
    private final UiLogger logger;
    private final int totalItems;
    private final int speedMs;
    private final AtomicBoolean running;
    private final AtomicInteger producedCount;

    // Shared item counter across all producers
    private static final AtomicInteger globalItemCounter = new AtomicInteger(0);

    private volatile String status;

    public ProducerWorker(int producerId, BoundedBuffer buffer, SimulationStats stats,
                          UiLogger logger, int totalItems, int speedMs,
                          AtomicBoolean running, AtomicInteger producedCount) {
        super("Producer-" + producerId);
        this.producerId = producerId;
        this.buffer = buffer;
        this.stats = stats;
        this.logger = logger;
        this.totalItems = totalItems;
        this.speedMs = speedMs;
        this.running = running;
        this.producedCount = producedCount;
        this.status = "Stopped";
    }

    @Override
    public void run() {
        status = "Running";
        logger.log("Producer " + producerId + " started");

        while (running.get() && producedCount.get() < totalItems) {
            try {
                // Simulate production time
                Thread.sleep(speedMs);

                // Check if we should stop
                if (!running.get() || producedCount.get() >= totalItems) break;

                // OS Concept: Producer waits when buffer is full.
                // The emptySlots semaphore handles this - if no empty slots,
                // acquire() blocks until a consumer frees one.
                // We detect the waiting state by checking if the semaphore has no permits.
                if (buffer.size() >= buffer.getCapacity()) {
                    status = "Waiting (buffer full)";
                    stats.incrementProducerWait();
                    logger.log("Producer " + producerId + " is waiting because buffer is full");
                }

                int itemId = globalItemCounter.incrementAndGet();
                BufferItem item = new BufferItem(itemId);

                // OS Concept: produce() uses semaphore acquire + mutex lock internally
                buffer.produce(item, producerId);
                producedCount.incrementAndGet();
                stats.incrementProduced();

                status = "Running";
                logger.log("Producer " + producerId + " produced item " + itemId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        status = "Stopped";
        logger.log("Producer " + producerId + " stopped");
    }

    public String getStatus() {
        return status;
    }

    /**
     * Reset the global item counter when starting a new simulation.
     */
    public static void resetGlobalCounter() {
        globalItemCounter.set(0);
    }
}
