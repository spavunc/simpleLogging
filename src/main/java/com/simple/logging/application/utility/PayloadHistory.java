package com.simple.logging.application.utility;

import com.simple.logging.application.payload.Payload;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing a history of {@link Payload} objects.
 * <p>
 * This class provides synchronized methods to add, remove, clear, and view log entries.
 * </p>
 */
@Slf4j
public class PayloadHistory {

    private PayloadHistory() {
        throw new IllegalStateException("Utility class");
    }

    private static final List<Payload> payloadHistoryList = new ArrayList<>();

    /**
     * Adds a log entry to the history.
     *
     * @param logEntry the {@link Payload} log entry to add
     */
    public static synchronized void addLog(Payload logEntry) {
        payloadHistoryList.add(logEntry);
    }

    /**
     * Removes a log entry from the history.
     *
     * @param logEntry the {@link Payload} log entry to remove
     */
    public static synchronized void removeLog(Payload logEntry) {
        payloadHistoryList.remove(logEntry);
        log.info("Removed log entry: {}", logEntry);
    }

    /**
     * Clears all log entries from the history.
     */
    public static synchronized void clearLog() {
        payloadHistoryList.clear();
        log.info("Cleared all log entries from the history.");
    }

    /**
     * Returns the list of log entries in the history.
     *
     * @return the list of {@link Payload} log entries
     */
    public static synchronized List<Payload> viewLogs() {
        return new ArrayList<>(payloadHistoryList);
    }
}