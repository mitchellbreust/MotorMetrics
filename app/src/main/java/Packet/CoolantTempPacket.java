package Packet;

public class CoolantTempPacket extends Packet {
    public CoolantTempPacket(PacketArgs packetArgs) {
        super(packetArgs);
    }

    @Override
    public String formatData() {
        Byte[] data = this.getData();
        if (data.length != 1) return "N/A";

        int temp = (data[0] & 0xFF) - 40;
        return String.valueOf(temp);
    }

    public String formatDataCelsius() {
        return this.formatData();
    }

    public String formatDataFahrenheit() {
        try {
            double celsius = Double.parseDouble(this.formatDataCelsius());
            double fahrenheit = celsius * 9 / 5 + 32;
            return String.format("%.1f", fahrenheit);
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }
}
