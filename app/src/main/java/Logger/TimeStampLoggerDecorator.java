package Logger;

import java.time.LocalTime;

public class TimeStampLoggerDecorator extends LoggerDecorator {

    public TimeStampLoggerDecorator(Logger logger) {
        super(logger);
    }

    @Override
    public void log(String content) {
        String PINK = "\u001B[35m";  // Magenta
        String RESET = "\u001B[0m";

        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();

        String timestamp = String.format("%02d:%02d:%02d", hour, minute, second);
        super.log(PINK + timestamp + RESET + " -- " + content);
    }

}
