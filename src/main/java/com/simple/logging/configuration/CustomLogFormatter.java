package com.simple.logging.configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {

  private static final DateTimeFormatter
    dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  public String format(LogRecord logRecord) {
    return dateTimeFormatter.format(LocalDateTime.now()) +
      " " +
      logRecord.getLevel().getName() +
      ": " +
      formatMessage(logRecord) +
      System.lineSeparator();
  }
}
