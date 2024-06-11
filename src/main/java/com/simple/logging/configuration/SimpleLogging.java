package com.simple.logging.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SimpleLoggingRegistrar.class)
public @interface SimpleLogging {
    int maxFileSize() default 50;

    int maxStringSize() default 5;

    String logFilePath() default "logs";

    String charset() default "UTF-8";

    int maxCacheHistoryLogs() default 100;

    int logRetentionLengthInDays() default 5;

    String logDeletionCronScheduler() default "0 0 0 * * ?";

    String applicationName() default "application";
}
