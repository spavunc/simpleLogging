package com.simple.logging.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SimpleLoggingConfiguration is a configuration class that sets up a logging dispatcher servlet
 * with configurable parameters for logging file size, string size, file path, charset, and cache history logs.
 */
@Configuration
@EnableScheduling
@Data
public class SimpleLoggingConfiguration implements WebMvcConfigurer {

    private final Integer maxFileSizeMb;
    private final Integer maxStringSizeMb;
    private final String logFilePath;
    private final String charset;
    private final Integer maxCacheHistoryLogs;
    private final Integer logRetentionLengthInDays;
    private final String logDeletionCronScheduler;
    private final String applicationName;

    /**
     * Constructs a new SimpleLoggingConfiguration with specified logging configurations.
     *
     * @param maxFileSizeMb            the maximum size of the log file in megabytes.
     * @param maxStringSizeMb          the maximum size of the request/response body to be logged in megabytes.
     * @param logFilePath              the directory path where log files will be stored.
     * @param charset                  the character encoding to be used for logging.
     * @param maxCacheHistoryLogs      the maximum number of logs to be cached in memory.
     * @param logRetentionLengthInDays length in days how long are the log files kept before deletion.
     * @param logDeletionCronScheduler cron scheduler how often are log files checked for deletion.
     * @param applicationName          name of your application.
     */
    public SimpleLoggingConfiguration(@Value("50") Integer maxFileSizeMb,
                                      @Value("5") Integer maxStringSizeMb,
                                      @Value("logs") String logFilePath,
                                      @Value("UTF-8") String charset,
                                      @Value("100") Integer maxCacheHistoryLogs,
                                      @Value("5") Integer logRetentionLengthInDays,
                                      @Value("0 0 0 * * ?") String logDeletionCronScheduler,
                                      @Value("application") String applicationName) {
        this.maxFileSizeMb = maxFileSizeMb;
        this.maxStringSizeMb = maxStringSizeMb;
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.maxCacheHistoryLogs = maxCacheHistoryLogs;
        this.logRetentionLengthInDays = logRetentionLengthInDays;
        this.logDeletionCronScheduler = logDeletionCronScheduler;
        this.applicationName = applicationName;
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
                charset, maxCacheHistoryLogs, logRetentionLengthInDays, logDeletionCronScheduler, applicationName);
    }
}