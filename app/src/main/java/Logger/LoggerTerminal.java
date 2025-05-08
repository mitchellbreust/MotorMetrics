package Logger;

public class LoggerTerminal implements Logger {
    
    public void log(String content) {
        System.out.println(content);
    }
}
