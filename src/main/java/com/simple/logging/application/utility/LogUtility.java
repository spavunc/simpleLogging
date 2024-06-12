package com.simple.logging.application.utility;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class LogUtility {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String FILE_NAME_REGEX = "-\\d{4}-\\d{2}-\\d{2}.*";

    private LogUtility() {
        throw new IllegalStateException("Utility class");
    }

    @Getter
    public static class UtilityObjects {
        private static String logFilePath;
        private static String applicationName;

        /**
         * Sets the configuration values.
         *
         * @param logFilePath     the path to the log files
         * @param applicationName the name of the application
         */
        public static void setObjects(String logFilePath, String applicationName) {
            UtilityObjects.logFilePath = logFilePath;
            UtilityObjects.applicationName = applicationName;
        }
    }

    /**
     * Moves a file to a new location.
     *
     * @param sourcePath the path of the file to move
     * @param targetPath the target path
     * @throws IOException if an I/O error occurs
     */
    public static synchronized void moveFile(Path sourcePath, Path targetPath) throws IOException {
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Moved file from {} to {}", sourcePath, targetPath);
        } catch (IOException e) {
            log.error("Failed to move file from {} to {}", sourcePath, targetPath, e);
            throw e;
        }
    }

    /**
     * Renames a file.
     *
     * @param sourcePath the path of the file to rename
     * @param newName    the new name of the file
     * @throws IOException if an I/O error occurs
     */
    public static synchronized void renameFile(Path sourcePath, String newName) throws IOException {
        Path targetPath = sourcePath.resolveSibling(newName);
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Renamed file from {} to {}", sourcePath, targetPath);
        } catch (IOException e) {
            log.error("Failed to rename file from {} to {}", sourcePath, targetPath, e);
            throw e;
        }
    }

    /**
     * Deletes a file.
     *
     * @param filePath the path of the file to delete
     * @throws IOException if an I/O error occurs
     */
    public static synchronized void deleteFile(Path filePath) throws IOException {
        try {
            Files.delete(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Compresses a log file into a ZIP archive.
     * Only .log files are allowed to be compressed.
     *
     * @param filePath the path of the file to compress
     * @throws IOException if an I/O error occurs
     */
    public static synchronized void zipFile(Path filePath) throws IOException {
        // Ensure only .log files are processed
        if (!filePath.toString().endsWith(".log")) {
            throw new IllegalArgumentException("Only .log files can be compressed");
        }

        String zipFileName = filePath.toString() + ".zip";
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(filePath.toFile())) {

            ZipEntry zipEntry = new ZipEntry(filePath.getFileName().toString());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
            log.info("Compressed file {} to {}", filePath, zipFileName);
        } catch (IOException e) {
            log.error("Failed to compress file: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Retrieves a list of all log files in the specified directory.
     *
     * @return a list of log files
     * @throws IOException if an I/O error occurs
     */
    public static synchronized List<File> getAllLogFiles() throws IOException {
        File logDir = new File(UtilityObjects.logFilePath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            log.warn("Log directory does not exist: " + UtilityObjects.logFilePath);
            return new ArrayList<>();
        }

        try {
            File[] logFiles = logDir.listFiles((dir, name) -> name.matches(UtilityObjects.applicationName + "-\\d{4}-\\d{2}-\\d{2}.*\\.log"));
            if (logFiles != null) {
                log.info("Retrieved {} log files from {}", logFiles.length, logDir);
                return List.of(logFiles);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Failed to retrieve log files from {}", logDir, e);
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves a list of log files between specified dates.
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return a list of log files between the specified dates
     * @throws IOException if an I/O error occurs
     */
    public static synchronized List<File> getLogFilesBetweenDates(LocalDate startDate, LocalDate endDate) throws IOException {
        List<File> filteredLogFiles = new ArrayList<>();
        File logDir = new File(UtilityObjects.logFilePath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            log.warn("Log directory does not exist: " + UtilityObjects.logFilePath);
            return filteredLogFiles;
        }

        if (logDir.exists() && logDir.isDirectory()) {
            try {
                File[] logFiles = logDir.listFiles((dir, name) -> name.matches(LogUtility.UtilityObjects.applicationName + "-\\d{4}-\\d{2}-\\d{2}.*"));
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        if (!logFile.getName().endsWith(".log"))
                            continue;
                        // Extract the date part from filename correctly
                        String datePart = logFile.getName().substring(
                                LogUtility.UtilityObjects.applicationName.length() + 1,
                                LogUtility.UtilityObjects.applicationName.length() + 11
                        );
                        LocalDate fileDate = LocalDate.parse(datePart, DATE_FORMATTER);
                        if ((fileDate.isEqual(startDate) || fileDate.isAfter(startDate)) && (fileDate.isEqual(endDate) || fileDate.isBefore(endDate))) {
                            filteredLogFiles.add(logFile);
                        }
                    }
                    log.info("Retrieved {} log files from {} between {} and {}", filteredLogFiles.size(), logDir, startDate, endDate);
                }
                return filteredLogFiles;
            } catch (Exception e) {
                log.error("Failed to retrieve log files from {} between {} and {}", logDir, startDate, endDate, e);
                throw new IOException("Failed to retrieve log files", e);
            }
        } else {
            log.warn("Log directory does not exist: {}", LogUtility.UtilityObjects.logFilePath);
            return filteredLogFiles;
        }
    }

    /**
     * Retrieves a list of log files for the specified date.
     *
     * @param date the date for which to retrieve log files
     * @return a list of log files for the specified date
     * @throws IOException if an I/O error occurs
     */
    public static synchronized List<File> getLogFilesForDate(LocalDate date) throws IOException {
        List<File> filteredLogFiles = new ArrayList<>();
        File logDir = new File(LogUtility.UtilityObjects.logFilePath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            log.warn("Log directory does not exist: {}", UtilityObjects.logFilePath);
            return filteredLogFiles;
        }

        if (logDir.exists() && logDir.isDirectory()) {
            try {
                File[] logFiles = logDir.listFiles((dir, name) -> name.matches(LogUtility.UtilityObjects.applicationName + "-\\d{4}-\\d{2}-\\d{2}.*"));
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        if (!logFile.getName().endsWith(".log"))
                            continue;
                        // Extract the date part from filename correctly
                        String datePart = logFile.getName().substring(
                                LogUtility.UtilityObjects.applicationName.length() + 1,
                                LogUtility.UtilityObjects.applicationName.length() + 11
                        );
                        LocalDate fileDate = LocalDate.parse(datePart, DATE_FORMATTER);
                        if (fileDate.isEqual(date)) {
                            filteredLogFiles.add(logFile);
                        }
                    }
                    log.info("Retrieved {} log files from {} for date {}", filteredLogFiles.size(), logDir, date);
                }
                return filteredLogFiles;
            } catch (Exception e) {
                log.error("Failed to retrieve log files from {} for date {}", logDir, date, e);
                throw new IOException("Failed to retrieve log files", e);
            }
        } else {
            log.warn("Log directory does not exist: {}", LogUtility.UtilityObjects.logFilePath);
            return filteredLogFiles;
        }
    }

    /**
     * Searches for a keyword in a log file and returns matching lines.
     * Only .log files can be accessed.
     *
     * @param filePath the path of the file to search
     * @param keyword  the keyword to search for
     * @return a list of matching lines
     * @throws IOException if an I/O error occurs
     */
    public static synchronized List<String> searchLogFile(Path filePath, String keyword) throws IOException {
        List<String> matchedLines = new ArrayList<>();
        // Ensure only .log files are accessed
        if (!filePath.toString().endsWith(".log")) {
            throw new IllegalArgumentException("Only .log files can be searched");
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(keyword)) {
                    matchedLines.add(line);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read log file: {}", filePath, e);
            throw e;
        }
        return matchedLines;
    }

    /**
     * Generates a file with the specified name and writes the provided list of strings to it.
     * Each string is written on a new line.
     *
     * @param fileName the name of the file to create
     * @param lines    the list of strings to write to the file
     * @throws IOException if an I/O error occurs during writing
     */
    public static void generateFileWithLines(String fileName, List<String> lines) throws IOException {
        // Ensure that fileName is not null or empty
        Objects.requireNonNull(fileName, "File name must not be null");
        Objects.requireNonNull(lines, "Lines must not be null");
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("File name must not be empty");
        }

        Path filePath = Path.of(UtilityObjects.logFilePath, fileName + ".log");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Failed to write to file: {}", filePath, e);
            throw e;
        }
    }
}
