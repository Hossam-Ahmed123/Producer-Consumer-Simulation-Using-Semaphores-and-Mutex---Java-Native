package com.os.producerconsumer.service;

import com.os.producerconsumer.model.BoundedBuffer;
import com.os.producerconsumer.model.BufferItem;
import com.os.producerconsumer.model.SimulationStats;
import com.os.producerconsumer.util.UiLogger;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orchestrates the entire Producer-Consumer simulation.
 * Manages thread lifecycle, buffer creation, and simulation state.
 *
 * OS Concepts:
 * - This service creates multiple threads demonstrating concurrent execution.
 * - Thread pooling and lifecycle management for the simulation.
 */
public class SimulationService {

    private BoundedBuffer buffer;
    private SimulationStats stats;
    private UiLogger logger;
    private final List<ProducerWorker> producers;
    private final List<ConsumerWorker> consumers;
    private AtomicBoolean running;
    private AtomicInteger producedCount;
    private AtomicInteger consumedCount;

    // Callback to refresh the UI after each action
    private Runnable onUpdate;

    private int bufferSize;
    private int producerCount;
    private int consumerCount;
    private int totalItems;
    private int producerSpeed;
    private int consumerSpeed;

    public SimulationService() {
        this.producers = new ArrayList<>();
        this.consumers = new ArrayList<>();
    }

    /**
     * Configures the simulation parameters.
     */
    public void configure(int bufferSize, int producerCount, int consumerCount,
                          int totalItems, int producerSpeed, int consumerSpeed,
                          UiLogger logger, Runnable onUpdate) {
        this.bufferSize = bufferSize;
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
        this.totalItems = totalItems;
        this.producerSpeed = producerSpeed;
        this.consumerSpeed = consumerSpeed;
        this.logger = logger;
        this.onUpdate = onUpdate;
        this.running = new AtomicBoolean(false);
        this.producedCount = new AtomicInteger(0);
        this.consumedCount = new AtomicInteger(0);
    }

    /**
     * Starts the simulation: creates buffer, stats, and all producer/consumer threads.
     */
    public void start() {
        if (running.get()) return;

        // Reset global item counter for fresh simulation
        ProducerWorker.resetGlobalCounter();

        buffer = new BoundedBuffer(bufferSize);
        stats = new SimulationStats();
        stats.setStatus("Running");
        running.set(true);
        producedCount.set(0);
        consumedCount.set(0);

        producers.clear();
        consumers.clear();

        logger.log("=== Simulation Started ===");
        logger.log("Buffer Size: " + bufferSize + ", Producers: " + producerCount
                + ", Consumers: " + consumerCount + ", Total Items: " + totalItems);

        // OS Concept: Create multiple producer threads
        for (int i = 1; i <= producerCount; i++) {
            ProducerWorker producer = new ProducerWorker(i, buffer, stats, logger,
                    totalItems, producerSpeed, running, producedCount);
            producers.add(producer);
            producer.start();
        }

        // OS Concept: Create multiple consumer threads
        for (int i = 1; i <= consumerCount; i++) {
            ConsumerWorker consumer = new ConsumerWorker(i, buffer, stats, logger,
                    totalItems, consumerSpeed, running, consumedCount);
            consumers.add(consumer);
            consumer.start();
        }

        // Start a monitoring thread to check for simulation completion
        Thread monitor = new Thread(() -> {
            while (running.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Check if all items have been produced and consumed
                boolean allProduced = producedCount.get() >= totalItems;
                boolean allConsumed = consumedCount.get() >= totalItems;

                if (allProduced && allConsumed) {
                    // OS Concept: Simulation stops when all items are processed
                    running.set(false);
                    stats.setStatus("Completed");
                    logger.log("=== Simulation Completed ===");
                    Platform.runLater(onUpdate);
                    break;
                }

                // Trigger UI refresh
                Platform.runLater(onUpdate);
            }
        }, "Monitor-Thread");
        monitor.setDaemon(true);
        monitor.start();

        Platform.runLater(onUpdate);
    }

    /**
     * Stops the simulation gracefully by setting the running flag to false.
     * Threads will finish their current iteration and exit.
     */
    public void stop() {
        if (!running.get()) return;
        running.set(false);
        stats.setStatus("Stopped");
        logger.log("=== Simulation Stopped by User ===");

        // Interrupt all threads to wake them from sleep/wait states
        for (ProducerWorker p : producers) {
            if (p.isAlive()) p.interrupt();
        }
        for (ConsumerWorker c : consumers) {
            if (c.isAlive()) c.interrupt();
        }

        Platform.runLater(onUpdate);
    }

    /**
     * Resets the simulation to its initial state.
     */
    public void reset() {
        stop();
        producers.clear();
        consumers.clear();
        buffer = null;
        if (stats != null) stats.reset();
        if (logger != null) logger.clear();
        producedCount = new AtomicInteger(0);
        consumedCount = new AtomicInteger(0);
        ProducerWorker.resetGlobalCounter();
        Platform.runLater(onUpdate);
    }

    /**
     * Returns a snapshot of the current buffer contents.
     */
    public BufferItem[] getBufferSnapshot() {
        if (buffer == null) return new BufferItem[0];
        return buffer.snapshot();
    }

    /**
     * Returns the buffer capacity.
     */
    public int getBufferCapacity() {
        if (buffer == null) return bufferSize;
        return buffer.getCapacity();
    }

    /**
     * Returns the current buffer size (number of items).
     */
    public int getCurrentBufferSize() {
        if (buffer == null) return 0;
        return buffer.size();
    }

    public SimulationStats getStats() {
        return stats;
    }

    public List<ProducerWorker> getProducers() {
        return producers;
    }

    public List<ConsumerWorker> getConsumers() {
        return consumers;
    }

    public boolean isRunning() {
        return running != null && running.get();
    }

    public int getTotalItems() {
        return totalItems;
    }
}
