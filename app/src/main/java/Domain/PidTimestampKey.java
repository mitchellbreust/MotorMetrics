package Domain;

import java.util.Objects;

public class PidTimestampKey {
    private final long timestamp;
    private final byte pid;

    public PidTimestampKey(long timestamp, byte pid) {
        this.timestamp = timestamp;
        this.pid = pid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte getPid() {
        return pid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PidTimestampKey)) return false;
        PidTimestampKey that = (PidTimestampKey) o;
        return timestamp == that.timestamp && pid == that.pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, pid);
    }

    @Override
    public String toString() {
        return "Key[pid=0x" + String.format("%02X", pid) + ", time=" + timestamp + "]";
    }
}

