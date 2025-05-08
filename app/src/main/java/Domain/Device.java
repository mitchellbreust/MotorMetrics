package Domain;

import Packet.Packet;
import java.util.HashMap;
import java.util.Map;

public class Device implements PacketObserver {
    private final int deviceId;
    private final Map<PidTimestampKey, Packet> packetHistory = new HashMap<>();

    public Device(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getId() {
        return deviceId;
    }

    public void update(Packet packet) {
        PidTimestampKey key = new PidTimestampKey(packet.getTimestamp(), packet.getDataPID());
        packetHistory.put(key, packet);
    }

    public Packet getLatestPacket(byte pid) {
        // Find the packet with the latest timestamp for the given PID
        return packetHistory.entrySet().stream()
                .filter(e -> e.getKey().getPid() == pid)
                .max((a, b) -> Long.compare(a.getKey().getTimestamp(), b.getKey().getTimestamp()))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    public String getLatestValue(byte pid) {
        Packet packet = getLatestPacket(pid);
        return packet != null ? packet.formatData() : "N/A";
    }

    public Map<PidTimestampKey, Packet> getAllPackets() {
        return packetHistory;
    }
}
