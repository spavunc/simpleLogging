package com.simple.logging.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class SimpleLoggingRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(@NotNull AnnotationMetadata importingClassMetadata, @NotNull BeanDefinitionRegistry registry) {
    AnnotationAttributes
      attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(SimpleLogging.class.getName()));
    assert attributes != null;
    Integer maxFileSize = attributes.getNumber("maxFileSize");
    Integer maxStringSize = attributes.getNumber("maxStringSize");
    String logFilePath = attributes.getString("logFilePath");

    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleLoggingConfiguration.class);
    builder.addConstructorArgValue(maxFileSize);
    builder.addConstructorArgValue(maxStringSize);
    builder.addConstructorArgValue(logFilePath);
    registry.registerBeanDefinition("simpleLoggerConfiguration", builder.getBeanDefinition());
  }

}
