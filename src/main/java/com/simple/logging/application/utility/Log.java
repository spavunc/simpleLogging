package com.simple.logging.application.utility;

import com.simple.logging.application.configuration.CustomFileHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class for setting up and managing application logging.
 */
public class Log {
    private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

    private final Integer maxFileSizeMb;
    private final String logFilePath;
    private final String charset;
    private final String applicationName;
    private final String loggingLevel;
    private final boolean logToConsole;
    private final Integer maxBackupFiles;

    /**
     * Constructs a new Log with specified logging configurations.
     *
     * @param maxFileSizeMb   the maximum size of the log file in megabytes.
     * @param logFilePath     the directory path where log files will be stored.
     * @param charset         the character encoding to be used for logging.
     * @param applicationName name of your application.
     * @param logToConsole    save Log.info() to console.
     * @param maxBackupFiles  maximum number of backup files.
     */
    public Log(Integer maxFileSizeMb, String logFilePath,
               String charset, String applicationName,
               String loggingLevel,
               boolean logToConsole,
               Integer maxBackupFiles) {
        this.maxFileSizeMb = maxFileSizeMb * 1024 * 1024; // Convert MB to bytes
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.applicationName = applicationName;
        this.loggingLevel = loggingLevel;
        this.logToConsole = logToConsole;
        this.maxBackupFiles = maxBackupFiles;
        setupLogger();
    }

    /**
     * Logs a message at the specified level.
     *
     * @param level   the logging level.
     * @param message the message to log.
     * @param args    optional arguments for the message.
     */
    public static void log(Level level, String message, Object... args) {
        if (args == null || args.length == 0) {
            LOGGER.log(level, message);
        } else {
            String formattedMessage = formatMessage(message, args);
            LOGGER.log(level, formattedMessage);
        }
    }

    /**
     * Logs an info message.
     *
     * @param message the message to log.
     * @param args    optional arguments for the message.
     */
    public static void info(String message, Object... args) {
        if (args == null || args.length == 0) {
            LOGGER.info(message);
        } else {
            String formattedMessage = formatMessage(message, args);
            LOGGER.info(formattedMessage);
        }
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log.
     * @param args    optional arguments for the message.
     */
    public static void error(String message, Object... args) {
        if (args == null || args.length == 0) {
            LOGGER.severe(message);
        } else {
            String formattedMessage = formatMessage(message, args);
            LOGGER.severe(formattedMessage);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log.
     * @param args    optional arguments for the message.
     */
    public static void warn(String message, Object... args) {
        if (args == null || args.length == 0) {
            LOGGER.warning(message);
        } else {
            String formattedMessage = formatMessage(message, args);
            LOGGER.warning(formattedMessage);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to log.
     * @param args    optional arguments for the message.
     */
    public static void debug(String message, Object... args) {
        if (args == null || args.length == 0) {
            LOGGER.fine(message);
        } else {
            String formattedMessage = formatMessage(message, args);
            LOGGER.fine(formattedMessage);
        }
    }

    /**
     * Formats a message with optional arguments.
     *
     * @param message the message to format.
     * @param args    the arguments to format the message with.
     * @return the formatted message.
     */
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

    /**
     * Sets the logging level of the logger.
     */
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
            FileHandler fileHandler = new CustomFileHandler(Paths.get(logFilePath), maxFileSizeMb, maxBackupFiles,
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
