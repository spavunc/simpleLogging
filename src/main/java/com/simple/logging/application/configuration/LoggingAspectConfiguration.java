package com.simple.logging.application.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.logging.application.utility.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspectConfiguration {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(com.simple.logging.application.annotation.LogAspect)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        // Log method name
        Log.info("Method '{}' is called", joinPoint.getSignature().toShortString());

        // Log method arguments
        if (joinPoint.getArgs().length > 0) {
            Log.info("Method arguments: {}", objectMapper.writeValueAsString(joinPoint.getArgs()));
        }

        // Proceed with method execution
        Object result = joinPoint.proceed();

        // Log method return value
        Log.info("Method return value: {}", objectMapper.writeValueAsString(result));

        return result;
    }
}
