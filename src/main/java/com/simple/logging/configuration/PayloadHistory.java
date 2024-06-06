package com.simple.logging.configuration;

import java.util.ArrayList;
import java.util.List;

public class PayloadHistory {
    private static List<Payload> payloadHistory = new ArrayList<>();

    public static synchronized void addLog(Payload log) {
        payloadHistory.add(log);
    }

    public static synchronized void removeLog(Payload log) {
        payloadHistory.remove(log);
    }

    public static synchronized void clearLog() {
        payloadHistory.clear();
    }

    public static synchronized List<Payload> viewLogs() {
        return payloadHistory;
    }
}