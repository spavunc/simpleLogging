package com.simple.logging.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableScheduling
public class DynamicLogRetentionScheduler {

    @Value("${retentionLengthInDays}")
    private Integer retentionLengthInDays;

    @Value("${logDeletionCronScheduler}")
    private String logDeletionCronScheduler;

    @Value("${logFilePath}")
    private String logFilePath;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    @PostConstruct
    public void scheduleLogDeletion() {
        taskScheduler.initialize();
        taskScheduler.schedule(this::applyLogRetentionPolicy, new CronTrigger(logDeletionCronScheduler));
    }

    public void applyLogRetentionPolicy() {
        File logDir = new File(logFilePath);
        if (!logDir.exists() || !logDir.isDirectory()) {
            System.out.println("Log directory does not exist: " + logFilePath);
            return;
        }

        File[] logFiles = logDir.listFiles((dir, name) -> name.matches("application-\\d{4}-\\d{2}-\\d{2}.*"));
        if (logFiles == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        for (File logFile : logFiles) {
            String datePart = logFile.getName().substring(12, 22); // Extract date part from filename
            LocalDate fileDate = LocalDate.parse(datePart, DATE_FORMATTER);
            long daysBetween = ChronoUnit.DAYS.between(fileDate, today);

            if (daysBetween > retentionLengthInDays) {
                try {
                    Files.delete(logFile.toPath());
                    System.out.println("Deleted log file: " + logFile.getName());
                } catch (Exception e) {
                    System.err.println("Failed to delete log file: " + logFile.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}
