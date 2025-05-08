package Packet;

import java.lang.Byte;

public class SpeedPacket extends Packet{
    public SpeedPacket(PacketArgs packetArgs) {
        super(packetArgs);
    }

    @Override
    public String formatData() {
        Byte[] data = this.getData();
        return this.byteArrayToNumStr(data);
    }

    public String formatDataKPH() {
        return this.formatData();
    }

    public String formatDataMPH() {
        try {
            double kph = Double.parseDouble(this.formatDataKPH());
            double mph = kph * 0.621371;
            return String.format("%.1f", mph);
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }

}
