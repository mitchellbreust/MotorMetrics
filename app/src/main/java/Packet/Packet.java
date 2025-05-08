package Packet;

import java.time.Instant;

/*
    ================================
    OBD-II PACKET STRUCTURE (Binary)
    ================================

    This packet is used to send OBD-II diagnostic data from an IoT device (e.g., Raspberry Pi) to a centralized TCP server.

    ┌────────────┬────────────┬──────────────────┬───────────────────────────────────┐
    │ Length (2) │ Device ID  │Timestamp 8 bytes │ DataMode 1 Byte│ PID 1 Byte│ Data N bytes
    └────────────┴────────────┴──────────────────┴───────────────────────────────────┘

    Timestamp (8 bytes):
        - Epoch time in milliseconds (long, big-endian)
        - Helps correlate data with time of reading
*/

public abstract class Packet {
    private final PacketArgs packetArgs;

    public Packet(PacketArgs packetArgs) {
        this.packetArgs = packetArgs;
    }

    public int getDeviceId() {
        return this.packetArgs.deviceId;
    }

    public byte getDataMode() {
        return this.packetArgs.dataMode;
    }

    public byte getDataPID() {
        return this.packetArgs.dataPID;
    }

    public Byte[] getData() {
        return this.packetArgs.data;
    }

    public long getTimestamp() {
        return this.packetArgs.timestamp;
    }

    public static long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }

    public String byteArrayToNumStr(Byte[] data) {
        if (data.length == 1) {
            byte b = data[0];
            int unsigned = b & 0xFF;
            return String.valueOf(unsigned); // 0–255
        }
        if (data.length == 2) {
            byte b1 = data[0];
            byte b2 = data[1];
            int unsigned = ((b1 & 0xFF) << 8) | (b2 & 0xFF);
            return String.valueOf(unsigned); // 0–65535
        }
        if (data.length == 4) {
            byte b1 = data[0];
            byte b2 = data[1];
            byte b3 = data[2];
            byte b4 = data[3];
            int i = ((b1 & 0xFF) << 24) |
                    ((b2 & 0xFF) << 16) |
                    ((b3 & 0xFF) << 8)  |
                    (b4 & 0xFF);
            return String.valueOf(i); // full 32-bit int
        }

        return "Unsupported byte length: " + data.length;
    }

    public abstract String formatData();
}
