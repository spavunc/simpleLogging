package com.simple.logging.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

    public DynamicLogRetentionScheduler(Integer logRetentionLengthInDays, String logDeletionCronScheduler, String logFIlePath,
                                        String applicationName) {
        this.logRetentionLengthInDays = logRetentionLengthInDays;
        this.logDeletionCronScheduler = logDeletionCronScheduler;
        this.logFilePath = logFIlePath;
        this.applicationName = applicationName;
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
            taskScheduler.schedule(this::applyLogRetentionPolicy, new CronTrigger(logDeletionCronScheduler));
        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression for log deletion: {}", logDeletionCronScheduler, e);
        }
    }

    /**
     * Applies the log retention policy by deleting log files that are older than the configured retention length.
     */
    public void applyLogRetentionPolicy() {
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

            if (daysBetween > logRetentionLengthInDays) {
                try {
                    Files.delete(logFile.toPath());
                    log.info("Deleted log file: " + logFile.getName());
                    logsDeletedCounter++;
                } catch (Exception e) {
                    log.error("Failed to delete log file: " + logFile.getName(), e);
                }
            }
        }

        log.info("Deletion of old log files complete. Deleted {} files.", logsDeletedCounter);
    }
}