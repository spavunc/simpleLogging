package com.simple.logging.configuration;

import javax.validation.constraints.NotNull;
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
        Integer maxFileSize = attributes.getNumber("maxFileSize");
        Integer maxStringSize = attributes.getNumber("maxStringSize");
        String logFilePath = attributes.getString("logFilePath");
        String charset = attributes.getString("charset");
        Integer maxCacheHistoryLogs = attributes.getNumber("maxCacheHistoryLogs");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleLoggingConfiguration.class);
        builder.addConstructorArgValue(maxFileSize);
        builder.addConstructorArgValue(maxStringSize);
        builder.addConstructorArgValue(logFilePath);
        builder.addConstructorArgValue(charset);
        builder.addConstructorArgValue(maxCacheHistoryLogs);
        registry.registerBeanDefinition("simpleLoggerConfiguration", builder.getBeanDefinition());
    }
}
