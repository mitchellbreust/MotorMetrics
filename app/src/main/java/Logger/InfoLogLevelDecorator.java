package Logger;

public class InfoLogLevelDecorator extends LoggerDecorator {

    public InfoLogLevelDecorator(Logger logger) {
        super(logger);
    }

    @Override
    public void log(String content) {
        String CYAN = "\u001B[36m";
        String RESET = "\u001B[0m";
        super.log(CYAN + "Info: " + RESET + content);
    }
}

