package com.simple.logging.application.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomLogProperties {

    CustomLogProperties() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadLocal<Map<String, String>> customProperties = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<List<String>> ignoredProperties = ThreadLocal.withInitial(
        ArrayList::new);

    public static void setCustomProperty(String key, String value) {
        customProperties.get().put(key, value);
    }

    public static void setCustomProperties(Map<String, String> properties) {
        customProperties.set(properties);
    }

    public static String getCustomProperty(String key) {
        return customProperties.get().get(key);
    }

    public static Map<String, String> getCustomProperties() {
        return new HashMap<>(customProperties.get());
    }

    public static void clearCustomProperties() {
        customProperties.remove();
    }

    public static void addIgnoredProperty(String prop) {
      ignoredProperties.get().add(prop);
    }

    public static List<String> getIgnoredProperties() {
      return new ArrayList<>(ignoredProperties.get());
    }

    public static void clearIgnoredProperties() {
      ignoredProperties.remove();
    }


}
