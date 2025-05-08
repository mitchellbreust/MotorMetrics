package Packet;

public class PacketArgs {
    protected final int deviceId;
    protected final long timestamp;
    protected final byte dataMode;
    protected final byte dataPID;
    protected final Byte[] data;

    public PacketArgs(int deviceId, byte dataMode, byte dataPID, Byte[] data, long timestamp) {
        this.deviceId = deviceId;
        this.dataMode = dataMode;
        this.dataPID = dataPID;
        this.data = data;
        
        this.timestamp = timestamp;
    }
} 