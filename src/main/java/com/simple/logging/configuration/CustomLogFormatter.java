package com.simple.logging.configuration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * CustomLogFormatter is a custom formatter for Java's logging framework.
 * It formats log messages with a timestamp, log level, and the log message itself.
 *
 * <p>The timestamp is formatted as "yyyy-MM-dd HH:mm:ss".</p>
 */
public class CustomLogFormatter extends Formatter {

    /**
     * DateTimeFormatter to format the timestamp in the log messages.
     */
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats a given LogRecord into a string.
     *
     * @param logRecord the log record to be formatted.
     * @return a formatted log message string.
     */
    @Override
    public String format(LogRecord logRecord) {
      String throwable = "";
      if (logRecord.getThrown() != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        logRecord.getThrown().printStackTrace(pw);
        pw.close();
        throwable = sw.toString();
      }
        return dateTimeFormatter.format(LocalDateTime.now()) +
                " " +
                logRecord.getLevel().getName() +
                ": " +
                formatMessage(logRecord) +
                throwable +
                System.lineSeparator();
    }
}