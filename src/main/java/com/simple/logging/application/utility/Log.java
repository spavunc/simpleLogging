package com.simple.logging.application.utility;

import com.simple.logging.application.configuration.CustomFileHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

    private final Integer maxFileSizeMb;
    private final String logFilePath;
    private final String charset;
    private final String applicationName;
    private final String loggingLevel;
    private final boolean logToConsole;

    /**
     * Constructs a new Log with specified logging configurations.
     *
     * @param maxFileSizeMb   the maximum size of the log file in megabytes.
     * @param logFilePath     the directory path where log files will be stored.
     * @param charset         the character encoding to be used for logging.
     * @param applicationName name of your application.
     * @param logToConsole    save Log.info() to console.
     */
    public Log(Integer maxFileSizeMb, String logFilePath,
               String charset, String applicationName,
               String loggingLevel,
               boolean logToConsole) {
        this.maxFileSizeMb = maxFileSizeMb * 1024 * 1024; // Convert MB to bytes
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.applicationName = applicationName;
        this.loggingLevel = loggingLevel;
        this.logToConsole = logToConsole;
        setupLogger();
    }

    public static void log(Level level, String message, Object... args) {
        if (args == null || args.length == 0) {
            LOGGER.log(level, message);
        } else {
            String formattedMessage = formatMessage(message, args);
            LOGGER.log(level, formattedMessage);
        }
    }

    private static String formatMessage(String message, Object... args) {
        // Check if the message contains `%s` for String.format
        if (message.contains("%s")) {
            return String.format(message, args);
        }

        // Handle `{}` placeholders
        String formattedMessage = message;
        for (Object arg : args) {
            formattedMessage = formattedMessage.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
        }
        return formattedMessage;
    }

    private void setLoggingLevel() {
        try {
            LOGGER.setLevel(Level.parse(loggingLevel));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Invalid logging level, logging level will be set to ALL");
        }
    }

    /**
     * Sets up the logger with a FileHandler for logging to a file.
     */
    private void setupLogger() {
        try {
            // Create FileHandler with size limit and rotating file pattern
            FileHandler fileHandler = new CustomFileHandler(Paths.get(logFilePath), maxFileSizeMb, 5,
                    Charset.forName(charset), applicationName);
            // Add the FileHandler to the logger.
            LOGGER.addHandler(fileHandler);
            setLoggingLevel();
            LOGGER.setUseParentHandlers(logToConsole);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to set up file logging", e);
        }
    }
}
