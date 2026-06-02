package com.os.producerconsumer.service;

import com.os.producerconsumer.model.BoundedBuffer;
import com.os.producerconsumer.model.BufferItem;
import com.os.producerconsumer.model.SimulationStats;
import com.os.producerconsumer.util.UiLogger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a Consumer thread in the simulation.
 *
 * OS Concept: Each Consumer runs in its own thread and consumes items
 * from the shared bounded buffer. Consumers must wait when the buffer is empty
 * (demonstrating semaphore-based synchronization).
 */
public class ConsumerWorker extends Thread {

    private final int consumerId;
    private final BoundedBuffer buffer;
    private final SimulationStats stats;
    private final UiLogger logger;
    private final int totalItems;
    private final int speedMs;
    private final AtomicBoolean running;
    private final AtomicInteger consumedCount;

    private volatile String status;

    public ConsumerWorker(int consumerId, BoundedBuffer buffer, SimulationStats stats,
                          UiLogger logger, int totalItems, int speedMs,
                          AtomicBoolean running, AtomicInteger consumedCount) {
        super("Consumer-" + consumerId);
        this.consumerId = consumerId;
        this.buffer = buffer;
        this.stats = stats;
        this.logger = logger;
        this.totalItems = totalItems;
        this.speedMs = speedMs;
        this.running = running;
        this.consumedCount = consumedCount;
        this.status = "Stopped";
    }

    @Override
    public void run() {
        status = "Running";
        logger.log("Consumer " + consumerId + " started");

        while (running.get() && consumedCount.get() < totalItems) {
            try {
                // Simulate consumption time
                Thread.sleep(speedMs);

                // Check if we should stop
                if (!running.get() || consumedCount.get() >= totalItems) break;

                // OS Concept: Consumer waits when buffer is empty.
                // The fullSlots semaphore handles this - if no items available,
                // acquire() blocks until a producer adds one.
                if (buffer.size() == 0) {
                    status = "Waiting (buffer empty)";
                    stats.incrementConsumerWait();
                    logger.log("Consumer " + consumerId + " is waiting because buffer is empty");
                }

                // OS Concept: consume() uses semaphore acquire + mutex lock internally
                BufferItem item = buffer.consume(consumerId);
                consumedCount.incrementAndGet();
                stats.incrementConsumed();

                status = "Running";
                logger.log("Consumer " + consumerId + " consumed item " + item.getId());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        status = "Stopped";
        logger.log("Consumer " + consumerId + " stopped");
    }

    public String getStatus() {
        return status;
    }
}
