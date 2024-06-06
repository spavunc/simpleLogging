package com.simple.logging.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SimpleLoggingConfiguration implements WebMvcConfigurer {

    private final Integer maxFileSize;
    private final Integer maxStringSize;
    private final String logFilePath;
    private final String charset;
    private final Integer maxCacheHistoryLogs;

    public SimpleLoggingConfiguration(@Value("${maxFileSize}") Integer maxFileSize,
                                      @Value("${maxStringSize}") Integer maxStringSize, @Value("${logFilePath}") String logFilePath,
                                      @Value("${charset}") String charset, @Value("100") Integer maxCacheHistoryLogs) {
        this.maxFileSize = maxFileSize;
        this.maxStringSize = maxStringSize;
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.maxCacheHistoryLogs = maxCacheHistoryLogs;
    }

    @Bean
    public ServletRegistrationBean<DispatcherServlet> dispatcherRegistration() {
        return new ServletRegistrationBean<>(dispatcherServlet());
    }

    @Bean(name = "loggingDispatcherServlet")
    public DispatcherServlet dispatcherServlet() {
        return new LoggableDispatcherServlet(maxFileSize, maxStringSize, logFilePath, charset, maxCacheHistoryLogs);
    }

}
