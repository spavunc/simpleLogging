package com.simple.logging.application.configuration;

import com.simple.logging.application.servlet.LoggableDispatcherServlet;
import com.simple.logging.scheduler.DynamicLogRetentionScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SimpleLoggingConfiguration is a configuration class that sets up a logging dispatcher servlet
 * with configurable parameters for logging file size, string size, file path, charset, and cache history logs.
 */
@Configuration
public class SimpleLoggingConfiguration implements WebMvcConfigurer {

    private final Integer maxFileSizeMb;
    private final Integer maxStringSizeMb;
    private final String logFilePath;
    private final String charset;
    private final Integer maxCacheHistoryLogs;
    private final Integer logRetentionLengthInDays;
    private final String logDeletionCronScheduler;
    private final String applicationName;
    private final boolean compressOldLogs;
    private final Integer zipOldLogFilesOlderThanDays;
    private final String zippedLogFilePath;

    /**
     * Constructs a new SimpleLoggingConfiguration with specified logging configurations.
     *
     * @param maxFileSizeMb               the maximum size of the log file in megabytes.
     * @param maxStringSizeMb             the maximum size of the request/response body to be logged in megabytes.
     * @param logFilePath                 the directory path where log files will be stored.
     * @param zippedLogFilePath           the directory path where zipped log files will be stored.
     * @param charset                     the character encoding to be used for logging.
     * @param maxCacheHistoryLogs         the maximum number of logs to be cached in memory.
     * @param logRetentionLengthInDays    length in days how long are the log files kept before deletion.
     * @param logDeletionCronScheduler    cron scheduler how often are log files checked for deletion.
     * @param applicationName             name of your application.
     * @param compressOldLogs             compress old logs into a ZIP archive.
     * @param zipOldLogFilesOlderThanDays Zip files older than x days.
     */
    public SimpleLoggingConfiguration(@Value("${maxFileSizeMb:50}") Integer maxFileSizeMb,
                                      @Value("${maxStringSizeMb:5}") Integer maxStringSizeMb,
                                      @Value("${logFilePath:logs/}") String logFilePath,
                                      @Value("${logFilePath:logs/}") String zippedLogFilePath,
                                      @Value("${charset:UTF-8}") String charset,
                                      @Value("${maxCacheHistoryLogs:100}") Integer maxCacheHistoryLogs,
                                      @Value("${logRetentionLengthInDays:15}") Integer logRetentionLengthInDays,
                                      @Value("${logDeletionCronScheduler:0 0 0 * * ?}") String logDeletionCronScheduler,
                                      @Value("${applicationName:application}") String applicationName,
                                      @Value("${compressOldLogs:true}") boolean compressOldLogs,
                                      @Value("${zipOldLogFilesOlderThanDays:4}") Integer zipOldLogFilesOlderThanDays) {
        this.maxFileSizeMb = maxFileSizeMb;
        this.maxStringSizeMb = maxStringSizeMb;
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.maxCacheHistoryLogs = maxCacheHistoryLogs;
        this.logRetentionLengthInDays = logRetentionLengthInDays;
        this.logDeletionCronScheduler = logDeletionCronScheduler;
        this.applicationName = applicationName;
        this.compressOldLogs = compressOldLogs;
        this.zipOldLogFilesOlderThanDays = zipOldLogFilesOlderThanDays;
        this.zippedLogFilePath = zippedLogFilePath;
    }

    /**
     * Registers the logging dispatcher servlet.
     *
     * @return the servlet registration bean for the logging dispatcher servlet.
     */
    @Bean
    public ServletRegistrationBean<DispatcherServlet> dispatcherRegistration() {
        return new ServletRegistrationBean<>(dispatcherServlet());
    }

    /**
     * Creates and configures a new instance of LoggableDispatcherServlet.
     *
     * @return the configured LoggableDispatcherServlet instance.
     */
    @Bean(name = "loggingDispatcherServlet")
    public DispatcherServlet dispatcherServlet() {
        return new LoggableDispatcherServlet(maxFileSizeMb, maxStringSizeMb, logFilePath,
                charset, maxCacheHistoryLogs, applicationName);
    }

    /**
     * Creates and configures a new instance of DynamicLogRetentionScheduler.
     *
     * @return the configured DynamicLogRetentionScheduler instance.
     */
    @Bean
    public DynamicLogRetentionScheduler dynamicLogRetentionScheduler() {
        return new DynamicLogRetentionScheduler(logRetentionLengthInDays, logDeletionCronScheduler,
                logFilePath, applicationName, compressOldLogs, zipOldLogFilesOlderThanDays, zippedLogFilePath);
    }

    /**
     * Creates and configures a new instance of LogUtility.
     *
     * @return the configured LogUtility instance.
     */
    @Bean
    public LogUtilityConfiguration logUtilityConfiguration() {
        return new LogUtilityConfiguration(logFilePath, applicationName);
    }
}