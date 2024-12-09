package org.verve.service;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Counter {

    private int count = 0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    // Private static instance
    private static final Counter INSTANCE = new Counter();

    // Private constructor to prevent instantiation
    private Counter() {}

    // Public method to provide access to the singleton instance
    public static Counter getInstance() {
        return INSTANCE;
    }

    // Method to increment the count
    public int increment() {
        writeLock.lock();
        try {
            count++;
            return count;
        } finally {
            writeLock.unlock();
        }
    }

    // Method to get the current count and reset it
    public int getCurrCountAndReset() {
        writeLock.lock();
        try {
            int currentCount = count;
            count = 0; // Reset the count
            return currentCount;
        } finally {
            writeLock.unlock();
        }
    }

    // Method to get the current count without resetting
    public int getCurrCount() {
        readLock.lock();
        try {
            return count;
        } finally {
            readLock.unlock();
        }
    }
}
