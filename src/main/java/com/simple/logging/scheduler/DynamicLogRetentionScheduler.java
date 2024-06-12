package com.simple.logging.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class that handles dynamic scheduling of log file deletion based on a retention policy.
 */
@Slf4j
public class DynamicLogRetentionScheduler {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    private final Integer logRetentionLengthInDays;
    private final String logDeletionCronScheduler;
    private final String logFilePath;
    private final String applicationName;
    private final boolean compressOldLogs;
    private final boolean deleteCompressedLogs;
    private final String zippedLogFilePath;

    public DynamicLogRetentionScheduler(Integer logRetentionLengthInDays, String logDeletionCronScheduler, String logFIlePath, String applicationName, boolean compressOldLogs, boolean deleteCompressedLogs, String zippedLogFilePath) {
        this.logRetentionLengthInDays = logRetentionLengthInDays;
        this.logDeletionCronScheduler = logDeletionCronScheduler;
        this.logFilePath = logFIlePath;
        this.applicationName = applicationName;
        this.compressOldLogs = compressOldLogs;
        this.deleteCompressedLogs = deleteCompressedLogs;
        this.zippedLogFilePath = zippedLogFilePath;
    }

    /**
     * Initializes the task scheduler and schedules the log deletion task according to the configured cron schedule.
     */
    @PostConstruct
    public void scheduleLogDeletion() {
        log.info("Initializing Task Scheduler...");
        if (logDeletionCronScheduler == null || logDeletionCronScheduler.isBlank()) {
            log.error("Cron expression for log deletion is empty or not set. Log deletion will not be scheduled.");
            return;
        }

        taskScheduler.initialize();
        log.info("Scheduling log deletion with cron expression: {}", logDeletionCronScheduler);
        try {
            taskScheduler.schedule(() -> {
                try {
                    applyLogRetentionPolicy();
                } catch (IOException e) {
                  log.error("Something went wrong when applying log retention policy: {}", logDeletionCronScheduler, e);
                }
            }, new CronTrigger(logDeletionCronScheduler));
        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression for log deletion: {}", logDeletionCronScheduler, e);
        }
    }

    /**
     * Applies the log retention policy by deleting log files that are older than the configured retention length.
     */
    public void applyLogRetentionPolicy() throws IOException {
        log.info("Initiating deletion of old log files...");

        File logDir = new File(logFilePath);
        if (!logDir.exists() || !logDir.isDirectory()) {
            log.warn("Log directory does not exist: " + logFilePath);
            return;
        }

        File[] logFiles = logDir.listFiles((dir, name) -> name.matches(applicationName + "-\\d{4}-\\d{2}-\\d{2}.*"));
        if (logFiles == null || logFiles.length == 0) {
            log.info("No log files found for deletion.");
            return;
        }

        Integer logsDeletedCounter = 0;
        LocalDate today = LocalDate.now();
        for (File logFile : logFiles) {
            String datePart = logFile.getName().substring(applicationName.length() + 1, applicationName.length() + 11); // Extract date part from filename
            LocalDate fileDate = LocalDate.parse(datePart, DATE_FORMATTER);
            long daysBetween = ChronoUnit.DAYS.between(fileDate, today);

            // Compress old logs into a ZIP archive, if compressLogFile returns true,
            // it means the ZIP has been created and original file can be deleted
            if (daysBetween >= 1 && compressOldLogs && compressLogFile(logFile)) {
                Files.delete(logFile.toPath());
                log.info("Deleted log file: " + logFile.getName());
                continue;
            }
            logsDeletedCounter = deleteOldLogs(daysBetween, logFile, logsDeletedCounter);
        }
        log.info("Deletion of old log files complete. Deleted {} files.", logsDeletedCounter);
    }

    /**
     * Handles the deletion of log files.
     * @param daysBetween how many days have passed between the creation of the log
     * @param logFile the log file to be deleted
     * @param logsDeletedCounter how many files have been deleted during the process so far
     */
    public Integer deleteOldLogs(long daysBetween, File logFile, Integer logsDeletedCounter) {
        Integer logsDeleted = logsDeletedCounter;
        if (daysBetween > logRetentionLengthInDays) {
            if (logFile.getName().endsWith(".zip")) {
                // If file is a ZIP and deleteCompressedLogs is true, delete it
                if (deleteCompressedLogs) {
                    try {
                        Files.delete(logFile.toPath());
                        log.info("Deleted compressed log file: {}", logFile.getName());
                        logsDeleted++;
                    } catch (Exception e) {
                        log.error("Failed to delete compressed log file: {}", logFile.getName(), e);
                    }
                }
            } else {
                // If file is not a ZIP, delete it
                try {
                    Files.delete(logFile.toPath());
                    log.info("Deleted log file: {}", logFile.getName());
                    logsDeleted++;
                } catch (Exception e) {
                    log.error("Failed to delete log file: {}", logFile.getName(), e);
                }
            }
        }
        return logsDeleted;
    }

    /**
     * Compresses a given log file into a ZIP archive.
     * <p>
     * This method reads the specified log file and writes it into a ZIP file
     * with the same name but with a ".zip" extension. The original log file is
     * not deleted within this method but can be deleted after calling this method if desired.
     * <p>
     *
     * @param logFile the log file to be compressed
     * @throws IOException if an I/O error occurs during the compression process
     */
    private boolean compressLogFile(File logFile) throws IOException {
        if (logFile.getName().endsWith(".zip")) return false;

        log.info("Compressing {}...", logFile.getName());
        String zipFileName = zippedLogFilePath + logFile.getName() + ".zip";
        try (FileOutputStream fos = new FileOutputStream(zipFileName); ZipOutputStream zos = new ZipOutputStream(fos); FileInputStream fis = new FileInputStream(logFile)) {

            ZipEntry zipEntry = new ZipEntry(logFile.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();

            log.info("Compression of {} complete.", logFile.getName());
        }

        return true;
    }
}