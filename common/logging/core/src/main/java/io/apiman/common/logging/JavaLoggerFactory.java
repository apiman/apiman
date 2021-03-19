//package io.apiman.common.logging;
//
//import java.util.logging.Logger;
//
//public class JavaLoggerFactory implements IDelegateFactory {
//
//    @Override
//    public IApimanLogger createLogger(String name) {
//        return new ApimanJavaLogger(Logger.getLogger(name));
//    }
//
//    @Override
//    public IApimanLogger createLogger(Class<?> klazz) {
//        return new ApimanJavaLogger(Logger.getLogger(klazz.getSimpleName()));
//    }
//
//    private static final class ApimanJavaLogger implements IApimanLogger {
//        private final Logger logger;
//
//        private ApimanJavaLogger(Logger logger) {
//            this.logger = logger;
//        }
//
//        @Override
//        public void info(String message) {
//            logger.info(message);
//        }
//
//        @Override
//        public void info(String message, Object... args) {
//            logger.info(message, args);
//        }
//
//        @Override
//        public void warn(String message) {
//            logger.warning(message);
//        }
//
//        @Override
//        public void warn(String message, Object... args) {
//
//        }
//
//        @Override
//        public void debug(String message) {
//            logger.fine(message);
//        }
//
//        @Override
//        public void debug(String message, Object... args) {
//
//        }
//
//        @Override
//        public void trace(String message) {
//            logger.finer(message);
//        }
//
//        @Override
//        public void trace(String message, Object... args) {
//
//        }
//
//        @Override
//        public void error(Throwable error) {
//            logger.log(System.Logger.Level.ERROR, "", error);
//
//        }
//
//        @Override
//        public void error(String message, Throwable error) {
//
//        }
//
//        @Override
//        public void error(Throwable error, String message, Object... args) {
//
//        }
//    }
//}
