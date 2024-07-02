package com.simple.logging.application.utility;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogUtility {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String FILE_NAME_REGEX = "-\\d{4}-\\d{2}-\\d{2}.*\\.log";
    private static final String DIRECTORY_NOT_EXIST = "Log directory does not exist: {}";

    private LogUtility() {
        throw new IllegalStateException("Utility class");
    }

    @Getter
    public static class UtilityObjects {
        private static String logFilePath;
        private static String applicationName;

        // Private constructor to hide the implicit public one
        private UtilityObjects() {
            throw new UnsupportedOperationException("UtilityObjects is a utility class and cannot be instantiated");
        }

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
     */
    public static synchronized void moveFile(Path sourcePath, Path targetPath) {
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Log.info("Moved file from {} to {}", sourcePath, targetPath);
        } catch (IOException e) {
            Log.error("Failed to move file from {} to {}", sourcePath, targetPath, e);
        }
    }

    /**
     * Renames a file.
     *
     * @param sourcePath the path of the file to rename
     * @param newName    the new name of the file
     */
    public static synchronized void renameFile(Path sourcePath, String newName) {
        Path targetPath = sourcePath.resolveSibling(newName);
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Log.info("Renamed file from {} to {}", sourcePath, targetPath);
        } catch (IOException e) {
            Log.error("Failed to rename file from {} to {}", sourcePath, targetPath, e);
        }
    }

    /**
     * Deletes a file.
     *
     * @param filePath the path of the file to delete
     */
    public static synchronized void deleteFile(Path filePath) {
        try {
            Files.delete(filePath);
            Log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            Log.error("Failed to delete file: {}", filePath, e);
        }
    }

    /**
     * Compresses a log file into a ZIP archive.
     * Only .log files are allowed to be compressed.
     *
     * @param filePath the path of the file to compress
     */
    public static synchronized void zipFile(Path filePath) {
        // Ensure only .log files are processed
        if (!filePath.getFileName().toString().endsWith(".log")) {
            throw new IllegalArgumentException("Only .log files can be compressed");
        }

        String zipFileName = filePath + ".zip";
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
            Log.info("Compressed file {} to {}", filePath, zipFileName);
        } catch (IOException e) {
            Log.error("Failed to compress file: {}", filePath, e);
        }
    }

    /**
     * Retrieves a list of all log files in the specified directory.
     *
     * @return a list of log files
     */
    public static synchronized List<File> getAllLogFiles() {
        File logDir = new File(UtilityObjects.logFilePath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            Log.warn("Log directory does not exist: " + UtilityObjects.logFilePath);
            return new ArrayList<>();
        }

        try {
            File[] logFiles = logDir.listFiles((dir, name) -> name.matches(UtilityObjects.applicationName + FILE_NAME_REGEX));
            if (logFiles != null) {
                Log.info("Retrieved {} log files from {}", logFiles.length, logDir);
                return List.of(logFiles);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            Log.error("Failed to retrieve log files from {}", logDir, e);
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves a list of log files between specified dates.
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return a list of log files between the specified dates
     */
    public static synchronized List<File> getLogFilesBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<File> filteredLogFiles = new ArrayList<>();
        File logDir = new File(UtilityObjects.logFilePath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            Log.warn("Log directory does not exist: " + UtilityObjects.logFilePath);
            return filteredLogFiles;
        }

        if (logDir.exists() && logDir.isDirectory()) {
            try {
                File[] logFiles = logDir.listFiles((dir, name) -> name.matches(UtilityObjects.applicationName + FILE_NAME_REGEX));
                if (logFiles != null) {
                    findLogsBetweenDates(startDate, endDate, logFiles, filteredLogFiles);
                    Log.info("Retrieved {} log files from {} between {} and {}", filteredLogFiles.size(), logDir, startDate, endDate);
                }
                return filteredLogFiles;
            } catch (Exception e) {
                Log.error("Failed to retrieve log files from {} between {} and {}", logDir, startDate, endDate, e);
            }
        } else {
            Log.warn(DIRECTORY_NOT_EXIST, UtilityObjects.logFilePath);
        }
        return filteredLogFiles;
    }

    private static void findLogsBetweenDates(LocalDate startDate, LocalDate endDate, File[] logFiles, List<File> filteredLogFiles) {
        for (File logFile : logFiles) {
            if (!logFile.getName().endsWith(".log"))
                continue;
            // Extract the date part from filename correctly
            String datePart = logFile.getName().substring(
                    UtilityObjects.applicationName.length() + 1,
                    UtilityObjects.applicationName.length() + 11
            );
            LocalDate fileDate = LocalDate.parse(datePart, DATE_FORMATTER);
            if ((fileDate.isEqual(startDate) || fileDate.isAfter(startDate)) && (fileDate.isEqual(endDate) || fileDate.isBefore(endDate))) {
                filteredLogFiles.add(logFile);
            }
        }
    }

    /**
     * Retrieves a list of log files for the specified date.
     *
     * @param date the date for which to retrieve log files
     * @return a list of log files for the specified date
     */
    public static synchronized List<File> getLogFilesForDate(LocalDate date) {
        List<File> filteredLogFiles = new ArrayList<>();
        File logDir = new File(UtilityObjects.logFilePath);

        if (!logDir.exists() || !logDir.isDirectory()) {
            Log.warn(DIRECTORY_NOT_EXIST, UtilityObjects.logFilePath);
            return filteredLogFiles;
        }

        if (logDir.exists() && logDir.isDirectory()) {
            try {
                File[] logFiles = logDir.listFiles((dir, name) -> name.matches(UtilityObjects.applicationName + FILE_NAME_REGEX));
                if (logFiles != null) {
                    findLogsForSpecificDate(date, logFiles, filteredLogFiles);
                    Log.info("Retrieved {} log files from {} for date {}", filteredLogFiles.size(), logDir, date);
                }
                return filteredLogFiles;
            } catch (Exception e) {
                Log.error("Failed to retrieve log files from {} for date {}", logDir, date, e);
            }
        } else {
            Log.warn(DIRECTORY_NOT_EXIST, UtilityObjects.logFilePath);
        }
        return filteredLogFiles;
    }

    /**
     * Find logs for a specific date in the given list of log files.
     *
     * @param  date              the specific date to search for
     * @param  logFiles          the array of log files to search through
     * @param  filteredLogFiles  the list to store filtered log files for the specific date
     */
    public static void findLogsForSpecificDate(LocalDate date, File[] logFiles, List<File> filteredLogFiles) {
        for (File logFile : logFiles) {
            if (!logFile.getName().endsWith(".log"))
                continue;

            String datePart = extractDatePart(logFile.getName());
            LocalDate fileDate = LocalDate.parse(datePart, DATE_FORMATTER);
            if (fileDate.isEqual(date)) {
                filteredLogFiles.add(logFile);
            }
        }
    }

    /**
     * Extracts the date part from a log file name.
     *
     * @param fileName the name of the file from which to extract the date part
     * @return the date part of the file name in the format "yyyy-MM-dd"
     * @throws StringIndexOutOfBoundsException if the file name is not long enough to contain the expected date part
     */
    public static String extractDatePart(String fileName) throws StringIndexOutOfBoundsException {
        return fileName.substring(
                UtilityObjects.applicationName.length() + 1,
                UtilityObjects.applicationName.length() + 11
        );
    }

    /**
     * Searches for a keyword in a log file and returns matching lines.
     * Only .log files can be accessed.
     *
     * @param filePath the path of the file to search
     * @param keyword  the keyword to search for
     * @return a list of matching lines
     */
    public static synchronized List<String> searchLogFile(Path filePath, String keyword) {
        List<String> matchedLines = new ArrayList<>();
        // Ensure only .log files are accessed
        if (!filePath.toString().endsWith(".log")) {
            throw new IllegalArgumentException("Only .log files can be searched");
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            long lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.contains(keyword)) {
                    matchedLines.add("[File]: " + filePath.getFileName() + "\n[Line No.]: " + lineNumber + "\n" + line + "\n\n");
                }
            }
        } catch (IOException e) {
            Log.error("Failed to read log file: {}", filePath, e);
        }
        return matchedLines;
    }

    /**
     * Generates a file with the specified name and writes the provided list of strings to it.
     * Each string is written on a new line.
     *
     * @param fileName the name of the file to create
     * @param lines    the list of strings to write to the file
     */
    public static void generateFileFromSearch(@NotNull String fileName, @NotEmpty List<String> lines) {
        Path filePath = Path.of(UtilityObjects.logFilePath, fileName + ".log");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            Log.error("Failed to write to file: {}", filePath, e);
        }
    }

    /**
     * Minifies a JSON string by removing unnecessary whitespaces and line breaks.
     *
     * @param jsonString the JSON string to minify
     * @return a compact JSON string
     */
    public static String minifyJsonString(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }

        StringBuilder minifiedJson = new StringBuilder();
        boolean inQuotes = false;

        for (char c : jsonString.toCharArray()) {
            switch (c) {
                case ' ', '\n', '\r', '\t' -> {
                    if (inQuotes) {
                        minifiedJson.append(c);
                    }
                }
                case '"' -> {
                    minifiedJson.append(c);
                    if (minifiedJson.length() == 1 || jsonString.charAt(
                        minifiedJson.length() - 2) != '\\') {
                        inQuotes = !inQuotes;
                    }
                }
                default -> minifiedJson.append(c);
            }
        }

        return minifiedJson.toString();
    }
}
