package com.simple.logging.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggableDispatcherServlet extends DispatcherServlet {

    private static final Logger LOGGER = Logger.getLogger(LoggableDispatcherServlet.class.getName());
    private final Integer maxStringSizeMb;
    private final Integer maxFileSizeMb;
    private final String logFilePath;
    private final String charset;
    private final Integer maxCacheHistoryLogs;

    public LoggableDispatcherServlet(int maxFileSize, int maxStringSize, String logFilePath,
                                     String charset, Integer maxCacheHistoryLogs) {
        this.maxFileSizeMb = maxFileSize * 1024 * 1024; // Convert MB to bytes
        this.maxStringSizeMb = maxStringSize * 1024 * 1024;
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.maxCacheHistoryLogs = maxCacheHistoryLogs;
        setupLogger();
    }

    private void setupLogger() {
        try {
            // Ensure logs directory exists
            Path logsPath = Paths.get(logFilePath);
            if (!Files.exists(logsPath)) {
                Files.createDirectories(logsPath);
            }

            // Define log filename with date
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dateTime = LocalDate.now().format(dtf);
            Path logFile = logsPath.resolve("application-" + dateTime + ".log");

            // Create FileHandler with size limit and rotating file pattern
            FileHandler fileHandler = new FileHandler(logFile.toString(), maxFileSizeMb, 1, true);
            fileHandler.setFormatter(new CustomLogFormatter());
            fileHandler.setEncoding(Charset.forName(charset, StandardCharsets.UTF_8).toString());

            // Add the FileHandler to the logger.
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL); // Log all levels
            LOGGER.setUseParentHandlers(false); // Disable console logging, if desired
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to set up file logging", e);
        }
    }

    @Override
    protected void doDispatch(@NotNull HttpServletRequest request, @NotNull
    HttpServletResponse response) {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        try {
            HandlerExecutionChain handler = getHandler(request);
            assert handler != null;
            executeLogDispatch(request, response, handler);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred - ", ex.getMessage());
        }
    }

    private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache, HandlerExecutionChain handler)
            throws IOException {

        Payload log = Payload.builder()
                .httpMethod(requestToCache.getMethod())
                .requestUrl(requestToCache.getRequestURL().toString())
                .httpStatus(responseToCache.getStatus())
                .requestHandler(handler.getHandler().toString())
                .build();

        LOGGER.info(() -> "HTTP METHOD - " + log.getHttpMethod());
        LOGGER.info("REQUEST URL - " + log.getRequestUrl());
        LOGGER.info(() -> "REQUEST HANDLER - " + log.getRequestHandler());
        LOGGER.info("HTTP STATUS - " + log.getHttpStatus());

        getRequestPayload(requestToCache, log);
        getResponsePayload(responseToCache, log);

        // Delete first element if the list is overflown
        if (PayloadHistory.viewLogs().size() > maxCacheHistoryLogs)
            PayloadHistory.viewLogs().removeFirst();

        PayloadHistory.addLog(log);
    }

    private void getRequestPayload(HttpServletRequest request, Payload log) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] byteArray = wrapper.getContentAsByteArray();
            printWrapper(byteArray, false, log);
        }
    }

    private void getResponsePayload(HttpServletResponse response, Payload log) throws IOException {
        updateResponse(response);
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] byteArray = wrapper.getContentAsByteArray();
            printWrapper(byteArray, true, log);
        }
    }

    private void printWrapper(byte[] byteArray, Boolean isPayloadResponse, Payload log) {
        if (byteArray.length > 0 && byteArray.length < maxStringSizeMb) {
            String jsonStringFromByteArray = new String(byteArray, StandardCharsets.UTF_8);
            LOGGER.info(jsonStringFromByteArray);

            // Check whether the payload is a response or a request
            if (Boolean.TRUE.equals(isPayloadResponse)) {
                log.setResponseBody(jsonStringFromByteArray);
            } else {
                log.setRequestBody(jsonStringFromByteArray);
            }
        } else if (byteArray.length > maxStringSizeMb) {
            LOGGER.info("Content too long!");
        }
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        assert responseWrapper != null;
        responseWrapper.copyBodyToResponse();
    }

    private boolean shouldLogRequest(HttpServletRequest request) throws Exception {
        HandlerExecutionChain handler = getHandler(request);
        if (handler != null && handler.getHandler() instanceof HandlerMethod handlerMethod) {
            handlerMethod = (HandlerMethod) handler.getHandler();
            if (handlerMethod.getBean().getClass().isAnnotationPresent(IgnoreLogging.class)) {
                return false;
            } else
                return !handlerMethod.getMethod().isAnnotationPresent(IgnoreLogging.class);
        }
        return false;
    }

    private void executeLogDispatch(HttpServletRequest request, HttpServletResponse response,
                                    @NotNull HandlerExecutionChain handler) throws Exception {
        try {
            super.doDispatch(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred - ", ex.getMessage());
            throw ex; // rethrow the exception after logging
        } finally {
            if (shouldLogRequest(request)) {
                log(request, response, handler);
            }
        }
    }

}
