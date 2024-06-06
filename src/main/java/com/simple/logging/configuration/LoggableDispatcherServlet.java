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

/**
 * LoggableDispatcherServlet is a custom implementation of Spring's DispatcherServlet that logs
 * incoming HTTP requests and outgoing HTTP responses.
 *
 * <p>It supports logging the requests and responses with configurable size limits, logging file
 * paths, charset, and cache history.</p>
 */
public class LoggableDispatcherServlet extends DispatcherServlet {

    private static final Logger LOGGER = Logger.getLogger(LoggableDispatcherServlet.class.getName());
    private final Integer maxStringSizeMb;
    private final Integer maxFileSizeMb;
    private final String logFilePath;
    private final String charset;
    private final Integer maxCacheHistoryLogs;

    /**
     * Constructs a new LoggableDispatcherServlet with specified logging configurations.
     *
     * @param maxFileSize         the maximum size of the log file in megabytes.
     * @param maxStringSize       the maximum size of the request/response body to be logged in megabytes.
     * @param logFilePath         the directory path where log files will be stored.
     * @param charset             the character encoding to be used for logging.
     * @param maxCacheHistoryLogs the maximum number of logs to be cached in memory.
     */
    public LoggableDispatcherServlet(int maxFileSize, int maxStringSize, String logFilePath,
                                     String charset, Integer maxCacheHistoryLogs) {
        this.maxFileSizeMb = maxFileSize * 1024 * 1024; // Convert MB to bytes
        this.maxStringSizeMb = maxStringSize * 1024 * 1024;
        this.logFilePath = logFilePath;
        this.charset = charset;
        this.maxCacheHistoryLogs = maxCacheHistoryLogs;
        setupLogger();
    }

    /**
     * Sets up the logger with a FileHandler for logging to a file.
     */
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
            fileHandler.setEncoding(Charset.forName(charset).toString());

            // Add the FileHandler to the logger.
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL); // Log all levels
            LOGGER.setUseParentHandlers(false); // Disable console logging, if desired
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to set up file logging", e);
        }
    }

    /**
     * Dispatches the request and response, and logs the request and response payloads.
     *
     * @param request  the HTTP request.
     * @param response the HTTP response.
     */
    @Override
    protected void doDispatch(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
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
            LOGGER.log(Level.SEVERE, "Exception occurred - {0}", ex.getMessage());
        }
    }

    /**
     * Logs the HTTP request and response details.
     *
     * @param requestToCache  the HTTP request to be logged.
     * @param responseToCache the HTTP response to be logged.
     * @param handler         the handler for the request.
     * @throws IOException if an input or output exception occurs.
     */
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
        if (PayloadHistory.viewLogs().size() >= maxCacheHistoryLogs) {
            PayloadHistory.viewLogs().removeFirst();
        }

        PayloadHistory.addLog(log);
    }

    /**
     * Retrieves and logs the request payload.
     *
     * @param request the HTTP request.
     * @param log     the payload log object.
     */
    private void getRequestPayload(HttpServletRequest request, Payload log) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] byteArray = wrapper.getContentAsByteArray();
            printWrapper(byteArray, false, log);
        }
    }

    /**
     * Retrieves and logs the response payload.
     *
     * @param response the HTTP response.
     * @param log      the payload log object.
     * @throws IOException if an input or output exception occurs.
     */
    private void getResponsePayload(HttpServletResponse response, Payload log) throws IOException {
        updateResponse(response);
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] byteArray = wrapper.getContentAsByteArray();
            printWrapper(byteArray, true, log);
        }
    }

    /**
     * Logs the request or response payload if within size limits.
     *
     * @param byteArray         the payload byte array.
     * @param isPayloadResponse indicates if the payload is a response.
     * @param log               the payload log object.
     */
    private void printWrapper(byte[] byteArray, boolean isPayloadResponse, Payload log) {
        if (byteArray.length > 0 && byteArray.length < maxStringSizeMb) {
            String jsonStringFromByteArray = new String(byteArray, StandardCharsets.UTF_8);
            LOGGER.info(jsonStringFromByteArray);

            // Check whether the payload is a response or a request
            if (isPayloadResponse) {
                log.setResponseBody(jsonStringFromByteArray);
            } else {
                log.setRequestBody(jsonStringFromByteArray);
            }
        } else if (byteArray.length > maxStringSizeMb) {
            LOGGER.info("Content too long!");
        }
    }

    /**
     * Updates the response by copying the body to the response.
     *
     * @param response the HTTP response.
     * @throws IOException if an input or output exception occurs.
     */
    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        assert responseWrapper != null;
        responseWrapper.copyBodyToResponse();
    }

    /**
     * Determines whether the request should be logged.
     *
     * @param request the HTTP request.
     * @return true if the request should be logged; false otherwise.
     * @throws Exception if an error occurs while determining if the request should be logged.
     */
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

    /**
     * Executes the dispatching of the request and logs the details.
     *
     * @param request  the HTTP request.
     * @param response the HTTP response.
     * @param handler  the handler for the request.
     * @throws Exception if an error occurs while dispatching the request.
     */
    private void executeLogDispatch(HttpServletRequest request, HttpServletResponse response,
                                    @NotNull HandlerExecutionChain handler) throws Exception {
        try {
            super.doDispatch(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred - {0}", ex.getMessage());
            throw ex; // rethrow the exception after logging
        } finally {
            if (shouldLogRequest(request)) {
                log(request, response, handler);
            }
        }
    }
}