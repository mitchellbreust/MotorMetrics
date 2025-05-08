package Logger;

public class SuccessLogLevelDecorator extends LoggerDecorator {

    public SuccessLogLevelDecorator(Logger logger) {
        super(logger);
    }

    @Override
    public void log(String content) {
        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";
        super.log(GREEN + "Success: " + RESET + content);
    }
}

