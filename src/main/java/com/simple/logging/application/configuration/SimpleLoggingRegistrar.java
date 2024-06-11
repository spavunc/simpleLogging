package com.simple.logging.application.configuration;

import com.simple.logging.application.annotation.SimpleLogging;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * SimpleLoggingRegistrar is responsible for registering bean definitions required for simple logging configuration.
 * It implements the ImportBeanDefinitionRegistrar interface.
 */
public class SimpleLoggingRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * Registers bean definitions for simple logging configuration.
     *
     * @param importingClassMetadata metadata of the importing class.
     * @param registry               the bean definition registry.
     */
    @Override
    public void registerBeanDefinitions(@NotNull AnnotationMetadata importingClassMetadata, @NotNull BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(SimpleLogging.class.getName()));
        assert attributes != null;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleLoggingConfiguration.class);
        builder.addConstructorArgValue(attributes.getNumber("maxFileSizeMb"));
        builder.addConstructorArgValue(attributes.getNumber("maxStringSizeMb"));
        builder.addConstructorArgValue(attributes.getString("logFilePath"));
        builder.addConstructorArgValue(attributes.getString("charset"));
        builder.addConstructorArgValue(attributes.getNumber("maxCacheHistoryLogs"));
        builder.addConstructorArgValue(attributes.getNumber("logRetentionLengthInDays"));
        builder.addConstructorArgValue(attributes.getString("logDeletionCronScheduler"));
        builder.addConstructorArgValue(attributes.getString("applicationName"));
        registry.registerBeanDefinition("simpleLoggerConfiguration", builder.getBeanDefinition());
    }
}
