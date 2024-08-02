package com.simple.logging.application.servlet;

import com.simple.logging.application.annotation.IgnoreLogging;
import com.simple.logging.application.model.CustomLogProperties;
import com.simple.logging.application.model.Payload;
import com.simple.logging.application.utility.Log;
import com.simple.logging.application.utility.LogUtility;
import com.simple.logging.application.utility.PayloadHistory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * LoggableDispatcherServlet is a custom implementation of Spring's DispatcherServlet that logs
 * incoming HTTP requests and outgoing HTTP responses.
 *
 * <p>It supports logging the requests and responses with configurable size limits, logging file
 * paths, charset, and cache history.</p>
 */
public class LoggableDispatcherServlet extends DispatcherServlet {
    private final Integer maxStringSizeMb;
    private final Integer maxCacheHistoryLogs;
    private final transient Charset charset;

    /**
     * Constructs a new LoggableDispatcherServlet with specified logging configurations.
     *
     * @param maxStringSizeMb     the maximum size of the request/response body to be logged in megabytes.
     * @param maxCacheHistoryLogs the maximum number of logs to be cached in memory.
     */
    public LoggableDispatcherServlet(Integer maxStringSizeMb, Integer maxCacheHistoryLogs,
        Charset charset) {
        this.maxStringSizeMb = maxStringSizeMb * 1024 * 1024;
        this.maxCacheHistoryLogs = maxCacheHistoryLogs;
        this.charset = charset;
    }

    /**
     * Dispatches the request and response, and logs the request and response payloads.
     *
     * @param request  the HTTP request.
     * @param response the HTTP response.
     */
    @Override
    protected void doDispatch(@NotNull HttpServletRequest request,
                              @NotNull HttpServletResponse response) {
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
            Log.error(String.format("Exception occurred - %s", ex.getMessage()), ex);
        } finally {
            // Clear custom properties after logging
            CustomLogProperties.clearCustomProperties();
        }
    }

    /**
     * Logs the HTTP request and response details.
     *
     * @param requestToCache  the HTTP request to be logged.
     * @param responseToCache the HTTP response to be logged.
     * @param handler         the handler for the request.
     */
    private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache,
                     HandlerExecutionChain handler) {

        String uuid = UUID.randomUUID().toString();
        Map<String, String> customProperties = LogUtility.getCustomLogPropertiesWithoutIgnored();

        Payload log = Payload.builder()
                .httpMethod(requestToCache.getMethod())
                .requestUrl(requestToCache.getRequestURL().toString())
                .httpStatus(responseToCache.getStatus())
                .requestHandler(handler.getHandler().toString())
                .timestamp(LocalDateTime.now())
                .uuid(uuid)
                .customProperties(customProperties)
                .build();

        Log.info(uuid + " HTTP METHOD - " + log.getHttpMethod());
        Log.info(uuid + " REQUEST URL - " + log.getRequestUrl());
        Log.info(uuid + " REQUEST HANDLER - " + log.getRequestHandler());
        Log.info(uuid + " HTTP STATUS - " + log.getHttpStatus());

        for (Map.Entry<String, String> entry : customProperties.entrySet()) {
            Log.info(uuid + " " + entry.getKey() + " - " + entry.getValue());
        }

        getRequestPayload(requestToCache, log);
        getResponsePayload(responseToCache, log);

        // Delete first element if the list is overflown
        if (PayloadHistory.viewLogs().size() >= maxCacheHistoryLogs) {
            Optional<Payload> firstPayload = PayloadHistory.viewLogs().stream().findFirst();
            firstPayload.ifPresent(PayloadHistory::removeLog);
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
     */
    private void getResponsePayload(HttpServletResponse response, Payload log) {
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
        if (byteArray.length < maxStringSizeMb) {
            String jsonString = LogUtility.removeIgnoredPropertiesFromJson(new String(byteArray, charset));
            String payloadMarker = isPayloadResponse ?
                    log.getUuid() + " RESPONSE BODY: {}" :
                    log.getUuid() + " REQUEST BODY: {}";
            if (!jsonString.isBlank()) {
                Log.info(payloadMarker, LogUtility.minifyJsonString(jsonString));
            }
            // Check whether the payload is a response or a request
            if (isPayloadResponse) {
                log.setResponseBody(LogUtility.minifyJsonString(jsonString));
            } else {
                log.setRequestBody(LogUtility.minifyJsonString(jsonString));
            }
        } else if (byteArray.length > maxStringSizeMb) {
            Log.warn("Content too long!");
        }
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
            Log.error(String.format("Exception occurred - %s", ex.getMessage()), ex);
        } finally {
            if (shouldLogRequest(request)) {
                log(request, response, handler);
            }
        }
    }
}