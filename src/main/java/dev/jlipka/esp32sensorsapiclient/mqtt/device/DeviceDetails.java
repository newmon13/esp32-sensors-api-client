package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import dev.jlipka.esp32sensorsapiclient.sensor.SensorReading;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;

import java.time.Instant;
import java.util.Map;


public class DeviceDetails {

    private final String deviceId;

    private final String ipAddress;

    private final Map<SensorType, SensorReading> sensors;

    private DeviceStatus status;

    private Instant lastHeartbeat;

    public DeviceDetails(String deviceId, String ipAddress, Map<SensorType, SensorReading> sensors) {
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.sensors = sensors;
        this.status = DeviceStatus.OFFLINE;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Map<SensorType, SensorReading> getSensors() {
        return sensors;
    }

    @Override
    public String toString() {
        return "DeviceDetails{" + "deviceId='" + deviceId + '\'' + ", ipAddress='" + ipAddress + '\'' + ", status=" + status + ", lastHeartbeat=" + lastHeartbeat + ", sensors=" + sensors + '}';
    }
}
