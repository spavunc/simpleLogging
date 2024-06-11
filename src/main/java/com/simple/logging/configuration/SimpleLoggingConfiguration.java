package com.simple.logging.configuration;

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
public class SimpleLoggingConfiguration implements WebMvcConfigurer {

    private final Integer maxFileSize;
    private final Integer maxStringSize;
    private final String logFilePath;
    private final String charset;
    private final Integer maxCacheHistoryLogs;
    private final Integer retentionLengthInDays;
    private final String logDeletionCronScheduler;

    /**
     * Constructs a new SimpleLoggingConfiguration with specified logging configurations.
     *
     * @param maxFileSize         the maximum size of the log file in megabytes.
     * @param maxStringSize       the maximum size of the request/response body to be logged in megabytes.
     * @param logFilePath         the directory path where log files will be stored.
     * @param charset             the character encoding to be used for logging.
     * @param maxCacheHistoryLogs the maximum number of logs to be cached in memory.
     */
    public SimpleLoggingConfiguration(@Value("${maxFileSize}") Integer maxFileSize,
                                      @Value("${maxStringSize}") Integer maxStringSize,
                                      @Value("${logFilePath}") String logFilePath,
                                      @Value("${charset}") String charset,
                                      @Value("${maxCacheHistoryLogs}") Integer maxCacheHistoryLogs,
                                      @Value("5") Integer retentionLengthInDays,
                                      @Value("0 0 0 * * ?") String logDeletionCronScheduler) {
        this.maxFileSize = maxFileSize;
        this.maxStringSize = maxStringSize;
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.maxCacheHistoryLogs = maxCacheHistoryLogs;
        this.retentionLengthInDays = retentionLengthInDays;
        this.logDeletionCronScheduler = logDeletionCronScheduler;
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
        return new LoggableDispatcherServlet(maxFileSize, maxStringSize, logFilePath, charset, maxCacheHistoryLogs, retentionLengthInDays, logDeletionCronScheduler);
    }
}