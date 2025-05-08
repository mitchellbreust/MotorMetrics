package DataStore;

import Packet.Packet;

import java.sql.*;
import java.util.*;

public class DataStore {
    private static DataStore INSTANCE = null;
    private Connection connection;

    private DataStore() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:obd2.db");
        connection.setAutoCommit(true);
        initializeDeviceTable();
    }

    public static DataStore getInstance() throws SQLException {
        if (INSTANCE == null) {
            INSTANCE = new DataStore();  // Now exception is handled here
        }
        return INSTANCE;
    }

    private void initializeDeviceTable() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS devices (device_id INTEGER PRIMARY KEY)");
    }

    public void registerDevice(int deviceId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR IGNORE INTO devices (device_id) VALUES (?)")) {
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deviceExists(int deviceId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM devices WHERE device_id = ?")) {
            stmt.setInt(1, deviceId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void storePacket(int deviceId, Packet packet) {
        String tableName = "pid_" + String.format("%02X", packet.getDataPID());
        System.out.println("formal table name: " + tableName);
        registerDevice(deviceId);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName +
                        " (timestamp INTEGER, device_id INTEGER, data TEXT, " +
                        "FOREIGN KEY(device_id) REFERENCES devices(device_id))");
            System.out.println("Table created or already exists: " + tableName);
        } catch (SQLException e) {
            System.err.println("Failed to create table: " + e.getMessage());
            e.printStackTrace();
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO " + tableName + " (timestamp, device_id, data) VALUES (?, ?, ?)")) {
            insert.setLong(1, packet.getTimestamp());
            insert.setInt(2, deviceId);
            insert.setString(3, packet.formatData());

            int rowsInserted = insert.executeUpdate();
            System.out.println("Rows inserted: " + rowsInserted);

            // ✅ Extra validation: Try to read it back right after insert
            try (PreparedStatement verify = connection.prepareStatement(
                    "SELECT timestamp, device_id, data FROM " + tableName + " WHERE timestamp = ? AND device_id = ?")) {
                verify.setLong(1, packet.getTimestamp());
                verify.setInt(2, deviceId);
                ResultSet rs = verify.executeQuery();
                if (rs.next()) {
                    System.out.println("✅ Verified insert: " +
                        "Time=" + rs.getLong("timestamp") +
                        ", Device=" + rs.getInt("device_id") +
                        ", Data=" + rs.getString("data"));
                } else {
                    System.err.println("❌ Insert verification failed — no row found.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Insert or verification failed: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public List<String> getPacketsByPID(byte pid) {
        List<String> results = new ArrayList<>();
        String tableName = "pid_" + String.format("%02X", pid);
        System.out.println("Going to get data with name: " + tableName);

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            while (rs.next()) {
                long ts = rs.getLong("timestamp");
                int deviceId = rs.getInt("device_id");
                String data = rs.getString("data");
                results.add("Time: " + ts + ", Device: " + deviceId + ", Data: " + data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public void clearAllData() {
        try (Statement stmt = connection.createStatement()) {
            // Drop all PID tables dynamically
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'pid_%'");
            List<String> tablesToDrop = new ArrayList<>();
            while (rs.next()) {
                tablesToDrop.add(rs.getString("name"));
            }
            for (String table : tablesToDrop) {
                stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
            }

            // Clear devices table
            stmt.executeUpdate("DELETE FROM devices");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasTable(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

} 

