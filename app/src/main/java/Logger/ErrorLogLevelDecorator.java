package Logger;

public class ErrorLogLevelDecorator extends LoggerDecorator {

    public ErrorLogLevelDecorator(Logger logger) {
        super(logger);
    }

    @Override
    public void log(String content) {
        String RED = "\u001B[31m";
        String RESET = "\u001B[0m";
        super.log(RED + "Error: " + RESET + content);
    }
}
