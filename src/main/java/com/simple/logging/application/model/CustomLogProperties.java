package com.simple.logging.application.model;

import java.util.HashMap;
import java.util.Map;

public class CustomLogProperties {

    CustomLogProperties() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadLocal<Map<String, String>> customProperties = ThreadLocal.withInitial(HashMap::new);

    public static void setProperty(String key, String value) {
        customProperties.get().put(key, value);
    }

    public static String getProperty(String key) {
        return customProperties.get().get(key);
    }

    public static Map<String, String> getProperties() {
        return new HashMap<>(customProperties.get());
    }

    public static void clear() {
        customProperties.remove();
    }
}
