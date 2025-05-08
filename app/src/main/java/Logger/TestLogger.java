package Logger;

public class TestLogger implements Logger {
    private String lastMessage = "";

    @Override
    public void log(String content) {
        this.lastMessage = content;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}

