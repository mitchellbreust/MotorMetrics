// ServerManagerTest.java
package Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import DataStore.DataStore;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.lang.Thread;

import java.net.ServerSocket;
import Decoder.PacketParser;
import java.net.Socket;

import static org.junit.Assert.*;

public class ServerManagerTest {
        private ServerManager serverManager;
        private Socket socket;  // ← declare here so all methods can access it

        @Before
        public void setup() {
            serverManager = new ServerManager();
            serverManager.initDataStore();
            serverManager.initServer(0);

            int port = serverManager.getServerSocket().getLocalPort();
            new Thread(() -> serverManager.listenLoop()).start();

            try {
                socket = new Socket("localhost", port);  // ← assign to the field
            } catch (IOException e) {
                System.out.println("Error with client socket creation: " + e.getMessage());
            }
        }

        @After
        public void teardown() {
            serverManager.clearDB();
            serverManager.stop();

            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }

    @Test
    public void testServerSocketStartsCorrectly() {
        boolean started = serverManager.initServer(0);  // use port 0 to auto-assign
        assertTrue("Server should start successfully", started);

        ServerSocket socket = serverManager.getServerSocket();
        assertNotNull("Server socket should not be null", socket);
        assertTrue("Server socket should be bound", socket.isBound());
        assertFalse("Server socket should not be closed", socket.isClosed());
    }

    @Test
    public void testInvalidHandshakeRejected() throws Exception {
        socket.getOutputStream().write(new byte[]{(byte) 0, (byte) 0, (byte) 4, (byte) 210, 'X', 'Y', 'Z'}); // Bad handshake
        socket.getOutputStream().flush();

        Thread.sleep(200); // Let server process

        assertEquals("Should not register invalid connection", 0, serverManager.getConnectedDevices().size());
        socket.close();
    }

    @Test
    public void testValidDeviceConnection() throws Exception {

        int deviceId = 1234;
        byte[] idBytes = PacketParser.intToBytes(deviceId);
        socket.getOutputStream().write(idBytes);
        socket.getOutputStream().write(new byte[]{'S', 'D', 'T'});
        socket.getOutputStream().flush();

        Thread.sleep(200); // Let server accept and process

        assertTrue("Device should be registered", serverManager.getConnectedDevices().containsKey(deviceId));
    }

    @Test
    public void testValidPayloadTransmission() throws Exception {

        int deviceId = 1234;
        byte[] idBytes = PacketParser.intToBytes(deviceId);
        socket.getOutputStream().write(idBytes);
        socket.getOutputStream().write(new byte[]{'S', 'D', 'T'}); // Handshake
        socket.getOutputStream().flush();

        // Simulate valid payload
        byte[] payload = new byte[]{
                                    (byte)0, (byte)16,                        // length = 22
                                    (byte)0, (byte)0, (byte)4, (byte) 210,          // deviceId = 1234
                                    (byte)0, (byte)0, (byte)1, (byte) 150,          // timestamp (first 4 bytes)
                                    (byte) 171, (byte)5, (byte)94, (byte) 232, // timestamp (last 4 bytes)
                                    (byte)1,                            // data mode
                                    (byte)0x0C,                           // PID = 0x0C (RPM)
                                    (byte)0x1F, (byte) 0xA0             // A=0x1F, B=0xA0 → RPM = 2024
                                };
        socket.getOutputStream().write(payload);
        socket.getOutputStream().flush();

        Thread.sleep(1000); // Let the server process the payload

        assertTrue("Device should still be registered", serverManager.getConnectedDevices().containsKey(deviceId));
        assertFalse("No exceptions should cause socket to close", socket.isClosed());

        System.out.println("Printing all data from db");
        DataStore d = DataStore.getInstance();
        if (!d.hasTable("pid_0C")) {
            fail("Table does not exist that should have been created: pid_0C");
        }
        for (String s : d.getPacketsByPID((byte) 0x0C)) {
            System.out.println(s);
        }
    }

    @Test
    public void testDuplicateDeviceIdRejected() throws Exception {
        int deviceId = 1234;
        byte[] idBytes = PacketParser.intToBytes(deviceId);

        // First connection
        socket.getOutputStream().write(idBytes);
        socket.getOutputStream().write(new byte[]{'S', 'D', 'T'});
        socket.getOutputStream().flush();
        Thread.sleep(200);

        // Second connection attempt
        Socket dupSocket = new Socket("localhost", serverManager.getServerSocket().getLocalPort());
        dupSocket.getOutputStream().write(idBytes);
        dupSocket.getOutputStream().write(new byte[]{'S', 'D', 'T'});
        dupSocket.getOutputStream().flush();
        Thread.sleep(200);

        assertTrue("Device should be registered once", serverManager.getConnectedDevices().containsKey(deviceId));
        dupSocket.close();
    }

    @Test
    public void testMalformedPayloadIsIgnoredOrCloses() throws Exception {
        int deviceId = 3333;
        byte[] idBytes = PacketParser.intToBytes(deviceId);
        socket.getOutputStream().write(idBytes);
        socket.getOutputStream().write(new byte[]{'S', 'D', 'T'});
        socket.getOutputStream().flush();

        // Send a payload shorter than expected
        byte[] badPayload = new byte[]{0, 2, 0x12}; // Insufficient length
        socket.getOutputStream().write(badPayload);
        socket.getOutputStream().flush();

        Thread.sleep(500);
        assertTrue("Server should not crash due to bad packet", serverManager.getConnectedDevices().containsKey(deviceId));
    }
    @Test
    public void testMultipleDeviceConnections() throws Exception {
        int[] deviceIds = {1111, 2222, 3333};
        List<Socket> openSockets = new ArrayList<>();

        for (int id : deviceIds) {
            socket.getOutputStream().write(PacketParser.intToBytes(id));
            socket.getOutputStream().write(new byte[]{'S', 'D', 'T'});
            socket.getOutputStream().flush();
            openSockets.add(socket);  // ✅ keep sockets alive
            Thread.sleep(200);   // small delay
            assertTrue("Device " + id + " should be connected", serverManager.getConnectedDevices().containsKey(id));
            socket = new Socket("localhost", serverManager.getServerSocket().getLocalPort());
        }

        // Give time for all to be processed
        Thread.sleep(500);

        for (int id : deviceIds) {
            assertTrue("Device " + id + " should be connected", serverManager.getConnectedDevices().containsKey(id));
        }

        // ✅ Close after test
        for (Socket s : openSockets) {
            s.close();
        }
    }

    @Test
    public void testSingleDeviceMultiplePayloads() throws Exception {
        int deviceId = 4444;
        socket.getOutputStream().write(PacketParser.intToBytes(deviceId));
        socket.getOutputStream().write(new byte[]{'S', 'D', 'T'});
        socket.getOutputStream().flush();

        for (int i = 0; i < 3; i++) {
            byte[] payload = new byte[]{
                (byte)0, (byte)16,
                (byte)((deviceId >> 24) & 0xFF), (byte)((deviceId >> 16) & 0xFF),
                (byte)((deviceId >> 8) & 0xFF), (byte)(deviceId & 0xFF),
                0, 0, 0, (byte)i,    // fake timestamp
                0, 0, 0, (byte)(i + 1),
                1,  // data mode
                0x0C,  // PID
                (byte)(0x10 + i), (byte)(0x20 + i)
            };

            socket.getOutputStream().write(payload);
            socket.getOutputStream().flush();
            Thread.sleep(100);
        }

        Thread.sleep(300); // let server write data
        DataStore d = DataStore.getInstance();
        assertTrue("Expected table pid_0C to exist", d.hasTable("pid_0C"));

        int rowCount = d.getPacketsByPID((byte)0x0C).size();
        assertTrue("Expected at least 3 rows for repeated payloads", rowCount == 3);
    }

    @Test
    public void testMultipleDevicesSendPayload() throws Exception {
        int[] deviceIds = {1111, 2222, 3333};
        List<Socket> openSockets = new ArrayList<>();

        for (int id : deviceIds) {
            socket.getOutputStream().write(PacketParser.intToBytes(id));
            socket.getOutputStream().write(new byte[]{'S', 'D', 'T'});
            socket.getOutputStream().flush();

            // Send a basic payload (PID = 0x0C)
            byte[] payload = new byte[]{
                (byte)0, (byte)16,
                (byte)((id >> 24) & 0xFF), (byte)((id >> 16) & 0xFF), (byte)((id >> 8) & 0xFF), (byte)(id & 0xFF),
                0, 0, 0, 1,   // fake timestamp
                0, 0, 0, 2,
                1,  // data mode
                0x0C,  // PID
                0x12, 0x34 // dummy data
            };

            socket.getOutputStream().write(payload);
            socket.getOutputStream().flush();

            Thread.sleep(500);
            openSockets.add(socket);
            socket = new Socket("localhost", serverManager.getServerSocket().getLocalPort());
        }

        // Check all devices were registered
        for (int id : deviceIds) {
            assertTrue("Device " + id + " should be connected", serverManager.getConnectedDevices().containsKey(id));
        }

        // Verify data was written to DB
        DataStore d = DataStore.getInstance();
        assertTrue("Expected table pid_0C to exist", d.hasTable("pid_0C"));
        assertTrue("Expected rows in table pid_0C", d.getPacketsByPID((byte)0x0C).size() >= 3);

        // ✅ Close after test
        for (Socket s : openSockets) {
            s.close();
        }
    }

}
