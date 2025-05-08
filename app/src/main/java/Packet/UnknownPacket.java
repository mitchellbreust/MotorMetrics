package Packet;

public class UnknownPacket extends Packet {
    public UnknownPacket(PacketArgs packetArgs) {
        super(packetArgs);
    }

    @Override
    public String formatData() {
        return "Unknown";
    } 
}
