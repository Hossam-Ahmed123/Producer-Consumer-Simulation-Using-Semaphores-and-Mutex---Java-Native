package com.os.producerconsumer.model;

/**
 * Represents a single item produced by a Producer and consumed by a Consumer.
 * Each item has a unique ID for tracking purposes.
 */
public class BufferItem {

    private final int id;

    public BufferItem(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
