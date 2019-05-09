package io.apiman.gateway.platforms.vertx3.logging;

import io.vertx.core.logging.Log4j2LogDelegate;
import io.vertx.core.logging.Logger;
import io.vertx.core.spi.logging.LogDelegate;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.apache.logging.log4j.message.MessageFormatMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * Modification of {@link Log4j2LogDelegate} to use {@link FormattedMessageFactory}.
 */
public class ApimanLog4j2LogDelegate implements LogDelegate {

    private ExtendedLogger logger;
    final static String FQCN = Logger.class.getCanonicalName();

    ApimanLog4j2LogDelegate(final String name) {
      logger = (ExtendedLogger) org.apache.logging.log4j.LogManager.getLogger(name, new FormattedMessageFactory());
    }

    @Override
    public boolean isWarnEnabled() { return logger.isWarnEnabled(); }

    @Override
    public boolean isInfoEnabled() {
      return logger.isInfoEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
      return logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
      return logger.isTraceEnabled();
    }

    @Override
    public void fatal(final Object message) {
      log(Level.FATAL, message);
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
      log(Level.FATAL, message, t);
    }

    @Override
    public void error(final Object message) {
      log(Level.ERROR, message);
    }

    @Override
    public void error(Object message, Object... params) {
      log(Level.ERROR, message.toString(), params);
    }

    @Override
    public void error(final Object message, final Throwable t) {
      log(Level.ERROR, message, t);
    }

    @Override
    public void error(Object message, Throwable t, Object... params) {
      log(Level.ERROR, message.toString(), t, params);
    }

    @Override
    public void warn(final Object message) {
      log(Level.WARN, message);
    }

    @Override
    public void warn(Object message, Object... params) {
      log(Level.WARN, message.toString(), params);
    }

    @Override
    public void warn(final Object message, final Throwable t) {
      log(Level.WARN, message, t);
    }

    @Override
    public void warn(Object message, Throwable t, Object... params) {
      log(Level.WARN, message.toString(), t, params);
    }

    @Override
    public void info(final Object message) {
      log(Level.INFO, message);
    }

    @Override
    public void info(Object message, Object... params) {
      log(Level.INFO, message.toString(), params);
    }

    @Override
    public void info(final Object message, final Throwable t) {
      log(Level.INFO, message, t);
    }

    @Override
    public void info(Object message, Throwable t, Object... params) {
      log(Level.INFO, message.toString(), t, params);
    }

    @Override
    public void debug(final Object message) {
      log(Level.DEBUG, message);
    }

    @Override
    public void debug(Object message, Object... params) {
      log(Level.DEBUG, message.toString(), params);
    }

    @Override
    public void debug(final Object message, final Throwable t) {
      log(Level.DEBUG, message, t);
    }

    @Override
    public void debug(Object message, Throwable t, Object... params) {
      log(Level.DEBUG, message.toString(), t, params);
    }

    @Override
    public void trace(final Object message) {
      log(Level.TRACE, message);
    }

    @Override
    public void trace(Object message, Object... params) {
      log(Level.TRACE, message.toString(), params);
    }

    @Override
    public void trace(final Object message, final Throwable t) {
      log(Level.TRACE, message.toString(), t);
    }

    @Override
    public void trace(Object message, Throwable t, Object... params) {
      log(Level.TRACE, message.toString(), t, params);
    }

    private void log(Level level, Object message) {
      log(level, message, null);
    }

    private void log(Level level, Object message, Throwable t) {
      logger.logIfEnabled(FQCN, level, null, message, t);
    }

    private void log(Level level, String message, Object... params) {
      logger.logIfEnabled(FQCN, level, null, message, params);
    }

    private void log(Level level, String message, Throwable t, Object... params) {
      logger.logIfEnabled(FQCN, level, null, new MessageFormatMessage(message, params), t);
    }

    @Override
    public Object unwrap() {
      return logger;
    }
}