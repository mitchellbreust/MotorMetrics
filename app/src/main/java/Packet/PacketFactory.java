package Packet;

public class PacketFactory {

    public static Packet create(PacketArgs packetArgs) {
        switch (packetArgs.dataPID) {
            case 0x0D: return new SpeedPacket(packetArgs);
            case 0x0C: return new RpmPacket(packetArgs);
            case 0x05: return new CoolantTempPacket(packetArgs);
            default:   return new UnknownPacket(packetArgs);
        }
    }
}