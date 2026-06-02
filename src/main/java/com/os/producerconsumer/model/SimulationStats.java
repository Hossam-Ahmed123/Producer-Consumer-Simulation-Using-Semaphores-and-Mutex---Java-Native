package com.os.producerconsumer.model;

/**
 * Thread-safe statistics container for tracking simulation metrics.
 * All counters are updated atomically using volatile and synchronized blocks.
 */
public class SimulationStats {

    private volatile int totalProduced;
    private volatile int totalConsumed;
    private volatile int producerWaitCount;
    private volatile int consumerWaitCount;
    private volatile String status;

    public SimulationStats() {
        this.totalProduced = 0;
        this.totalConsumed = 0;
        this.producerWaitCount = 0;
        this.consumerWaitCount = 0;
        this.status = "Stopped";
    }

    public synchronized void incrementProduced() {
        totalProduced++;
    }

    public synchronized void incrementConsumed() {
        totalConsumed++;
    }

    public synchronized void incrementProducerWait() {
        producerWaitCount++;
    }

    public synchronized void incrementConsumerWait() {
        consumerWaitCount++;
    }

    public int getTotalProduced() {
        return totalProduced;
    }

    public int getTotalConsumed() {
        return totalConsumed;
    }

    public int getProducerWaitCount() {
        return producerWaitCount;
    }

    public int getConsumerWaitCount() {
        return consumerWaitCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Resets all counters back to their initial state.
     */
    public synchronized void reset() {
        totalProduced = 0;
        totalConsumed = 0;
        producerWaitCount = 0;
        consumerWaitCount = 0;
        status = "Stopped";
    }
}
