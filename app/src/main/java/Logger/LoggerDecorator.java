package Logger;

public abstract class LoggerDecorator implements Logger {
    protected Logger logger;

    public LoggerDecorator(Logger logger) {
        this.logger = logger;
    }

    public void log(String content) {
        this.logger.log(content);
    }
}
