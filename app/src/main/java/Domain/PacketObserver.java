package Domain;

import Packet.Packet;

public interface PacketObserver {
    void update(Packet packet);
    int getId();
}