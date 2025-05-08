package Logger;

public class WarningLogLevelDecorator extends LoggerDecorator {

    public WarningLogLevelDecorator(Logger logger) {
        super(logger);
    }

    @Override
    public void log(String content) {
        String YELLOW = "\u001B[33m";
        String RESET = "\u001B[0m";
        super.log(YELLOW + "Warning: " + RESET + content);
    }
    
}
