package com.simple.logging.application.payload;

import java.util.ArrayList;
import java.util.List;

public class PayloadHistory {

    private PayloadHistory() {
      throw new IllegalStateException("Utility class");
    }

    private static final List<Payload> payloadHistoryList = new ArrayList<>();

    public static synchronized void addLog(Payload log) {
        payloadHistoryList.add(log);
    }

    public static synchronized void removeLog(Payload log) {
        payloadHistoryList.remove(log);
    }

    public static synchronized void clearLog() {
        payloadHistoryList.clear();
    }

    public static synchronized List<Payload> viewLogs() {
        return payloadHistoryList;
    }
}