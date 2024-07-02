package com.simple.logging.application.configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

public class CustomFileHandler extends FileHandler {

    private static final String FILE_EXTENSION = ".log";
    private final int maxFileSize;
    private final int maxBackupFiles;
    private final Path filePath;
    private final String applicationName;
    private final Charset charset;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CustomFileHandler(Path filePath, int maxFileSize, int maxBackupFiles, Charset charset, String applicationName) throws IOException {
        super();
        this.maxFileSize = maxFileSize;
        this.maxBackupFiles = maxBackupFiles;
        this.filePath = filePath;
        this.applicationName = applicationName;
        this.charset = charset;
        configureHandler();
    }

    /**
     * Configures the file handler.
     *
     * @throws IOException if an I/O error occurs
     */
    private void configureHandler() throws IOException {
        setEncoding(charset.toString());
        setFormatter(new CustomLogFormatter());
        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath);
        }
        Path currentLogFile = getLatestLogFile(filePath);
        setOutputStream(Files.newOutputStream(currentLogFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
    }

    /**
     * Get the latest log file or create a new one if it doesn't exist.
     *
     * @param logsPath the path to the directory containing the log files
     * @return the path to the latest log file
     * @throws IOException if an I/O error occurs
     */
    private Path getLatestLogFile(Path logsPath) throws IOException {
        String dateTime = LocalDate.now().format(dtf);
        String logFileNamePrefix = applicationName + "-" + dateTime;

        // Find latest log file or create a new one
        int index = 0;
        Path currentLogFile = logsPath.resolve(logFileNamePrefix + FILE_EXTENSION);
        while (Files.exists(currentLogFile)) {
            if (Files.size(currentLogFile) < maxFileSize) {
                break; // Use existing file if not exceeded max size
            }
            index++;
            currentLogFile = logsPath.resolve(logFileNamePrefix + "-" + index + FILE_EXTENSION);
        }

        // Rotate old files if necessary
        rotateLogFiles(logsPath, logFileNamePrefix);

        return currentLogFile;
    }

    /**
     * Rotate log files by shifting them to the next index, deleting the oldest file if necessary.
     *
     * @param  logsPath           the path to the directory containing the log files
     * @param  logFileNamePrefix  the prefix of the log file names
     * @throws IOException        if an I/O error occurs
     */
    private void rotateLogFiles(Path logsPath, String logFileNamePrefix) throws IOException {
        for (int i = maxBackupFiles - 1; i >= 0; i--) {
            Path oldFile = logsPath.resolve(logFileNamePrefix + "-" + i + FILE_EXTENSION);
            if (Files.exists(oldFile)) {
                if (i == maxBackupFiles - 1) {
                    Files.deleteIfExists(oldFile); // Delete the oldest file
                } else {
                    Path newFile = logsPath.resolve(logFileNamePrefix + "-" + (i + 1) + FILE_EXTENSION);
                    Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    @Override
    public synchronized void publish(LogRecord logRecord) {
        super.publish(logRecord);
        flush();
    }
}
