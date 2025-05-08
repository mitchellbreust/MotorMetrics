package Packet;

public class RpmPacket extends Packet {
    public RpmPacket(PacketArgs packetArgs) {
        super(packetArgs);
    }

    @Override
    public String formatData() {
        Byte[] data = this.getData();
        if (data.length != 2) return "N/A";

        int rpm = ((data[0] & 0xFF) << 8 | (data[1] & 0xFF)) / 4;
        return String.valueOf(rpm);
    }

    public String formatDataRPM() {
        return this.formatData();
    }
}

