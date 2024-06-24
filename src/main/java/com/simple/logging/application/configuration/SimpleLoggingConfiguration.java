package com.simple.logging.application.configuration;

import com.simple.logging.application.servlet.LoggableDispatcherServlet;
import com.simple.logging.application.utility.Log;
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
    private final String loggingLevel;
    private final boolean logToConsole;

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
     * @param zipOldLogFilesOlderThanDays zip files older than x days.
     * @param loggingLevel                logging level that corresponds to java.util.logging.Level.
     * @param logToConsole               save Log.info() to console.
     */
    public SimpleLoggingConfiguration(@Value("${maxFileSizeMb}") Integer maxFileSizeMb,
                                      @Value("${maxStringSizeMb}") Integer maxStringSizeMb,
                                      @Value("${logFilePath}") String logFilePath,
                                      @Value("${logFilePath}") String zippedLogFilePath,
                                      @Value("${charset}") String charset,
                                      @Value("${maxCacheHistoryLogs}") Integer maxCacheHistoryLogs,
                                      @Value("${logRetentionLengthInDays}") Integer logRetentionLengthInDays,
                                      @Value("${logDeletionCronScheduler}") String logDeletionCronScheduler,
                                      @Value("${applicationName}") String applicationName,
                                      @Value("${compressOldLogs}") boolean compressOldLogs,
                                      @Value("${zipOldLogFilesOlderThanDays}") Integer zipOldLogFilesOlderThanDays,
                                      @Value("${loggingLevel}") String loggingLevel,
                                      @Value("${logToConsole}") boolean logToConsole) {
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
        this.loggingLevel = loggingLevel;
        this.logToConsole = logToConsole;
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
        return new LoggableDispatcherServlet(maxStringSizeMb, maxCacheHistoryLogs);
    }

    /**
     * Creates and configures a new instance of Log.
     *
     * @return the configured Log instance.
     */
    @Bean
    public Log log() {
        return new Log(maxFileSizeMb, logFilePath,
                charset, applicationName, loggingLevel, logToConsole);
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