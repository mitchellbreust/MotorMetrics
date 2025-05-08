package Server;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.net.ServerSocket;
import java.net.Socket;
import Logger.*;
import DataStore.DataStore;
import Decoder.PacketParser;
import Domain.Device;

public class ServerManager implements ConnectionObserver {
    private final Logger errLog = LoggerFactory.createErrorLogger();
    private final Logger warningLog = LoggerFactory.createWarningLogger();
    private final Logger successLog = LoggerFactory.createSuccessLogger();
    private final Map<Integer, ClientConnection> connectedDevices = new HashMap<>();
    private DataStore dataStore;
    private ServerSocket server;

    public boolean start(int port) {
        if (!initServer(port)) return false;
        if (!initDataStore()) return false;
        listenLoop();
        return true;
    }

    public boolean initServer(int port) {
        try {
            this.server = new ServerSocket(port);
            successLog.log("Start server on port " + server.getLocalPort());
            return true;
        } catch (IOException e) {
            errLog.log("Got exception when trying to init server: " + e.getMessage());
            return false;
        }
    }

    public boolean initDataStore() {
        try {
            this.dataStore = DataStore.getInstance();
            return true;
        } catch (SQLException e) {
            errLog.log("Got exception when trying to establish connection/create sqlite db: " + e.getMessage());
            return false;
        }
    }

    public void listenLoop() {
        while (!this.server.isClosed()) {
            try {
                Socket socket = server.accept();
                socket.setSoTimeout(10000);
                handleConnection(socket);
            } catch (IOException e) {
                warningLog.log("Got exception when trying to accept socket (moving on): " + e.getMessage());
            }
        }
    }

    public void handleConnection(Socket socket) {
        try {
            InputStream stream = socket.getInputStream();
            byte[] idBytes = readBytes(stream, 4);
            if (idBytes == null) {
                warningLog.log("Stream closed before reading full 4-byte device ID.");
                socket.close();
                return;
            }

            int deviceId = PacketParser.bytesToInt(idBytes);
            if (connectedDevices.containsKey(deviceId)) {
                warningLog.log("Client already has a connection to server, breaking this one.");
                socket.close();
                return;
            }

            if (!validateHandshake(stream)) {
                warningLog.log("Did not receive the correct validation chars to allow connection.");
                socket.close();
                return;
            }

            if (!this.dataStore.deviceExists(deviceId)) {
                this.dataStore.registerDevice(deviceId);
            }

            Device device = new Device(deviceId);
            ClientConnection clientCon = new ClientConnection(socket, device, this.dataStore, this);
            connectedDevices.put(deviceId, clientCon);
            new Thread(clientCon).start();
            successLog.log("Accepted device " + deviceId + " and started thread.");
        } catch (IOException e) {
            warningLog.log("Got exception when trying to establish socket: " + e.getMessage());
            try { socket.close(); } catch (IOException ex) { errLog.log("Error closing socket: " + ex.getMessage()); }
        }
    }

    public byte[] readBytes(InputStream stream, int count) throws IOException {
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            int r = stream.read();
            if (r == -1) return null;
            bytes[i] = (byte) r;
        }
        return bytes;
    }

    public boolean validateHandshake(InputStream stream) throws IOException {
        return stream.read() == 'S' && stream.read() == 'D' && stream.read() == 'T';
    }

    public void stop() {
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        } catch (IOException e) {
            errLog.log("Error closing server socket: " + e.getMessage());
        }
    }

    public ServerSocket getServerSocket() {
        return this.server;
    }

    public Map<Integer, ClientConnection> getConnectedDevices() {
        return connectedDevices;
    }

    public void clearDB() {
        this.dataStore.clearAllData();
    }

    @Override
    public void onClientDisconnected(int deviceId) {
        connectedDevices.remove(deviceId);
        successLog.log("Cleaned up device " + deviceId + " after disconnect.");
    }
} 
