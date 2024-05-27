package com.simple.logging.configuration;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SimpleLoggingConfiguration implements WebMvcConfigurer{
  @Bean
  public ServletRegistrationBean<DispatcherServlet> dispatcherRegistration() {
    return new ServletRegistrationBean<>(dispatcherServlet());
  }

  @Bean(name = "loggingDispatcherServlet")
  public DispatcherServlet dispatcherServlet() {
    return new LoggableDispatcherServlet();
  }

}
