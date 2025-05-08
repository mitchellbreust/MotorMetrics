package Decoder;

import Packet.PacketArgs;
import java.lang.Byte;

public class PacketParser {
    public static PacketArgs parse(byte[] rawData) {
        if (rawData.length < 12) {
            throw new IllegalArgumentException("Packet too short");
        }

        int offset = 0;
        // Device ID (4 bytes -> int)
        int deviceId = ((rawData[offset] & 0xFF) << 24) |
                       ((rawData[offset + 1] & 0xFF) << 16) |
                       ((rawData[offset + 2] & 0xFF) << 8) |
                       (rawData[offset + 3] & 0xFF);
        offset += 4;

        // Timestamp (8 bytes -> long)
        long timestamp = 0;
        for (int i = 0; i < 8; i++) {
            timestamp = (timestamp << 8) | (rawData[offset + i] & 0xFF);
        }
        offset += 8;

        // Data Mode (1 byte)
        byte dataMode = rawData[offset++];

        // PID (1 byte)
        byte dataPID = rawData[offset++];

        // Remaining = Data (variable length)
        int dataLength = rawData.length - offset; // length field excludes the first 2 bytes
        Byte[] data = new Byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            data[i] = rawData[offset + i];
        }

        return new PacketArgs(deviceId, dataMode, dataPID, data, timestamp);
    }

    public static byte[] intToBytes(int val) {
        return new byte[] {
            (byte)((val >> 24) & 0xFF),
            (byte)((val >> 16) & 0xFF),
            (byte)((val >> 8) & 0xFF),
            (byte)(val & 0xFF)
        };
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            throw new IllegalArgumentException("Byte array must be exactly 4 bytes long.");
        }
        return ((bytes[0] & 0xFF) << 24) |
            ((bytes[1] & 0xFF) << 16) |
            ((bytes[2] & 0xFF) << 8)  |
            (bytes[3] & 0xFF);
        }
}
