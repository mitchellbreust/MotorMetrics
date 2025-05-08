package Server;

import Decoder.PacketParser;
import Packet.PacketArgs;
import Packet.Packet;
import Packet.PacketFactory;
import Domain.PacketObserver;
import java.io.InputStream;
import java.net.Socket;

import DataStore.DataStore;

import java.io.IOException;

public class ClientConnection implements Runnable {
    private final Socket clientSocket;
    private final PacketObserver packetObserver;
    private final DataStore dataStore;
    private ConnectionObserver connectionObserver;

    public ClientConnection(Socket clientSocket, PacketObserver packetObserver, DataStore dataStore, ConnectionObserver connectionObserver) {
        this.clientSocket = clientSocket;
        this.packetObserver = packetObserver;
        this.dataStore = dataStore;
        this.connectionObserver = connectionObserver;
    }

    private void notify(byte[] bytes) {
        System.out.println("About to process bytes to packet object");
        PacketArgs packetArgs = PacketParser.parse(bytes);
        Packet packet = PacketFactory.create(packetArgs);
        System.out.println("Packet object info after passed:\n" + packet.getDataPID() + "\n" + packet.getDeviceId() + "\n" + String.valueOf(packet.formatData()));
        this.packetObserver.update(packet);
        this.dataStore.storePacket(packetObserver.getId(), packet);
    }

    @Override
    public void run() {
        System.out.println("🧵 ClientConnection thread started for socket: " + clientSocket);
        try (InputStream stream = this.clientSocket.getInputStream()) {
            while (!this.clientSocket.isClosed()) {
                int raw1 = stream.read();
                if (raw1 == -1) {
                    System.out.println("Bad data or client doesw not want to send more data, exit client connection");
                    break;
                }

                int raw2 = stream.read();
                if (raw2 == -1) {
                    System.out.println("Bad data or client doesw not want to send more dataBad data, exit client connection");
                    break;
                }

                short lengthPayload = (short) ((raw1 << 8) | raw2);
                if (lengthPayload > 1024) {
                    System.out.println("Payload to long, exit client connection");
                    break;
                }

                int toRead = lengthPayload;
                byte[] buffer = new byte[toRead];
                int read = 0;
                while (read < toRead) {
                    int bytesRead = stream.read(buffer, read, toRead - read);
                    if (bytesRead == -1) {
                        System.out.println("When trying to read payload, past length, got bad data. Exit client connection");
                        break;
                    }
                    read += bytesRead;
                }
                if (read != toRead) {
                    System.out.println("Was not able to find full payload");
                    break;
                }

                this.notify(buffer);
            }
            System.out.println("Making sure socket is properly closed");
        } catch (IOException e) {
            System.err.println("Client connection failed: " + e.getMessage());
        }finally {
            try { this.clientSocket.close(); } catch (IOException ignored) {}
            connectionObserver.onClientDisconnected(packetObserver.getId());
        }
    }

}
