package com.simple.logging.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Configuration class that handles dynamic scheduling of log file deletion based on a retention policy.
 */
@Component
@Slf4j
public class DynamicLogRetentionScheduler implements EnvironmentAware {
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    private Environment environment;

    @Autowired
    public DynamicLogRetentionScheduler(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Initializes the task scheduler and schedules the log deletion task according to the configured cron schedule.
     */
    @PostConstruct
    public void scheduleLogDeletion() {
        log.info("Initializing Task Scheduler...");
        String logDeletionCronScheduler = environment.getProperty("logDeletionCronScheduler", String.class);
        taskScheduler.initialize();
        log.info("Scheduling log deletion with cron expression: {}", logDeletionCronScheduler);
        taskScheduler.schedule(this::applyLogRetentionPolicy, new CronTrigger(logDeletionCronScheduler));
    }

    /**
     * Applies the log retention policy by deleting log files that are older than the configured retention length.
     */
    public void applyLogRetentionPolicy() {
        log.info("Initiating deletion of old log files...");

        Integer logRetentionLengthInDays = environment.getProperty("logRetentionLengthInDays", Integer.class);
        String applicationName = environment.getProperty("applicationName", String.class);
        String logFilePath = environment.getProperty("logFilePath", String.class);

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