package com.simple.logging.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class SimpleLoggingRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(@NotNull AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    // Register LoggingWorldConfiguration class
    BeanDefinitionBuilder
      builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleLoggingConfiguration.class);
    registry.registerBeanDefinition("simpleLoggerConfiguration", builder.getBeanDefinition());
  }

}
