# MotorMetrics

**MotorMetrics** is a custom-built OBD2 data analysis platform designed to receive, decode, and interpret vehicle diagnostics data over a TCP connection. This project centers around a lightweight Java-based TCP server tailored for receiving structured data from microcontrollers like the ESP32. The system is extensible and will evolve into a full-stack analytics tool with a frontend dashboard for visualizations, insights, and diagnostic control.

---

## 🚗 Features (Current & Planned)

### ✅ Built
- **Custom TCP Server**: A robust server in Java built from scratch with its own binary data structure, parser, and handling logic.
- **Device Connection Handling**: Validates and tracks multiple OBD2 devices through a handshake protocol.
- **Data Decoding & Storage**: Parses custom packet formats and persists incoming data to an SQLite database.
- **Basic Security & Abuse Protection**:
  - Prevents data ingestion from unregistered or improperly handshaking devices
  - Enforces payload size limits to reduce risk of buffer overflows or simple DDoS attempts
  - Drops malformed or suspicious connections early
- **Expandable Protocol**: Designed for flexibility—other clients/devices can implement the same protocol to communicate with the server.

### 🧠 Coming Soon
- **Real-Time Data Analysis**: Highlight unusual engine behavior or performance patterns using custom rule sets.
- **Frontend Dashboard**: Graphs and displays vehicle data in real-time, including speed, RPM, temperatures, and more.
- **Diagnostic Code Management**: View and clear stored DTCs (Diagnostic Trouble Codes) from the frontend UI.
- **ESP32 Firmware Integration**: Reference implementation for ESP32 microcontroller to stream OBD2 data wirelessly.
- **AI/ML-Based Anomaly Detection**: Use machine learning to surface patterns and suggest maintenance actions from live data streams.

---

## 🔌 Protocol Design

Data is transmitted using a **custom binary packet structure**, optimized for embedded-to-server communication. This includes:
- Device ID
- Timestamp
- Mode/PID
- Encoded sensor values

The `PacketParser` and `PacketFactory` handle encoding/decoding of this structure. The design ensures minimal overhead and fast parsing, ideal for low-power microcontrollers like the ESP32.

---

## 🔐 TCP Security Considerations

The server includes basic protective measures to guard against misuse:
- **Strict device handshake verification** before accepting payloads
- **Size limits on packets** to avoid excessive memory allocation
- **Single connection per device ID** to prevent flooding
- Future plans include rate limiting, authentication tokens, and signature validation

---

## 🔧 Microcontroller Support

MotorMetrics is built with ESP32 compatibility in mind, and soon we’ll include a C++ reference client for:
- OBD2 polling via UART/CAN
- Packaging and sending data to the server over Wi-Fi

You’re also encouraged to implement your own hardware client using our protocol as a baseline!

---

## 📊 Vision

MotorMetrics will become a modular and intelligent OBD2 analytics platform capable of:
- Helping enthusiasts and mechanics diagnose vehicle issues faster
- Visualizing trends in vehicle behavior over time
- Supporting plug-and-play setups with common microcontrollers
- Offering machine learning insights and maintenance forecasts in future versions


