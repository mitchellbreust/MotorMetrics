package Server;

import Logger.*;

public class LoggerFactory {

    public static Logger createWarningLogger() {
        return new WarningLogLevelDecorator(
                   new TimeStampLoggerDecorator(
                       new LoggerTerminal()
                   )
               );
    }

    public static Logger createErrorLogger() {
        return new ErrorLogLevelDecorator(
                   new TimeStampLoggerDecorator(
                       new LoggerTerminal()
                   )
               );
    }

    public static Logger createInfoLogger() {
        return new InfoLogLevelDecorator(
                   new TimeStampLoggerDecorator(
                       new LoggerTerminal()
                   )
               );
    }

    public static Logger createSuccessLogger() {
        return new SuccessLogLevelDecorator(
                   new TimeStampLoggerDecorator(
                       new LoggerTerminal()
                   )
               );
    }
}
