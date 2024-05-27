package com.simple.logging.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.*;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoggableDispatcherServlet extends DispatcherServlet {

  private static final Logger LOGGER = Logger.getLogger(LoggableDispatcherServlet.class.getName());
  private static final Integer MAX_STRING_SIZE = 5120;

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
      LOGGER.error(ex.getStackTrace());
    }
  }

  private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache, HandlerExecutionChain handler) {
    LOGGER.info(requestToCache.getMethod());
    LOGGER.info(requestToCache.getRequestURL());
    getRequestPayload(requestToCache);
    LOGGER.info(handler.getHandler().toString());
    LOGGER.info(responseToCache.getStatus());
    getResponsePayload(responseToCache);
  }

  private void getRequestPayload(HttpServletRequest request) {
    ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
    if (wrapper != null) {
      byte[] byteArray = wrapper.getContentAsByteArray();
      printWrapper(byteArray);
    }
  }

  private void getResponsePayload(HttpServletResponse response) {
    ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
    if (wrapper != null) {
      byte[] byteArray = wrapper.getContentAsByteArray();
      printWrapper(byteArray);
    }
  }

  private void printWrapper(byte[] byteArray) {
    if (byteArray.length > 0 && byteArray.length < MAX_STRING_SIZE) {
      String jsonStringFromByteArray = new String(byteArray, StandardCharsets.UTF_8);
      LOGGER.info(jsonStringFromByteArray);
    } else if (byteArray.length > MAX_STRING_SIZE) {
      LOGGER.info("Content too long!");
    }
  }

  private void updateResponse(HttpServletResponse response) throws IOException {
    ContentCachingResponseWrapper responseWrapper =
      WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
    assert responseWrapper != null;
    responseWrapper.copyBodyToResponse();
  }

  private void logException(Exception ex) {
    LOGGER.error("Exception occurred: " + ex.getMessage(), ex);
  }

  private void executeLogDispatch(HttpServletRequest request, HttpServletResponse response,
    @NotNull HandlerExecutionChain handler) throws Exception {
    try {
      super.doDispatch(request, response);
    } catch (Exception ex) {
      logException(ex);
      throw ex; // rethrow the exception after logging
    } finally {
      log(request, response, handler);
      updateResponse(response);
    }
  }

}
