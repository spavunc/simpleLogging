package com.simple.logging.application.configuration;

import com.simple.logging.application.utility.LogUtility;
import jakarta.annotation.PostConstruct;

public class LogUtilityConfiguration {
    private final String logFilePath;
    private final String applicationName;

    public LogUtilityConfiguration(String logFilePath, String applicationName) {
        this.logFilePath = logFilePath;
        this.applicationName = applicationName;
    }

    @PostConstruct
    public void init() {
        LogUtility.UtilityObjects.setObjects(logFilePath, applicationName);
    }

}
