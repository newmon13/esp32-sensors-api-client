package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import dev.jlipka.esp32sensorsapiclient.mqtt.discovery.DiscoveryMessage;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorReading;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;
import dev.jlipka.esp32sensorsapiclient.sensor.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

public class Device implements Observer {

    private final Logger logger = LoggerFactory.getLogger(Device.class);

    private final DeviceDetails deviceDetails;

    private final DeviceStatusMonitor deviceStatusMonitor;

    private Observer parentObserver;

    public Device(DeviceDetails deviceDetails) {
        this.deviceDetails = deviceDetails;
        this.deviceStatusMonitor = new DeviceStatusMonitor();
        this.deviceStatusMonitor.addObserver(this);
    }

    public static Device from(DiscoveryMessage message) {
        Map<SensorType, SensorReading> sensors = message.sensors()
                .stream()
                .collect(toMap(sensorType -> sensorType, sensorType -> new SensorReading(0, 0, Severity.NONE)));
        DeviceDetails deviceDetails = new DeviceDetails(message.deviceId(), message.ipAddress(), sensors);
        return new Device(deviceDetails);
    }

    public DeviceDetails getDeviceDetails() {
        return deviceDetails;
    }

    public void setParentObserver(Observer observer) {
        this.parentObserver = observer;
    }

    public void updateHeartbeat(HeartbeatMessage heartbeatMessage) {
        if (deviceStatusMonitor.updateHeartbeat(heartbeatMessage.timestamp())) {
            deviceDetails.setLastHeartbeat(heartbeatMessage.timestamp());
            parentObserver.update();
            logger.info("Updated heartbeat of {} to {}", deviceDetails.getDeviceId(), deviceDetails.getLastHeartbeat());
        } else {
            logger.info("Failed to update heartbeat of {} to {}", deviceDetails.getDeviceId(), deviceDetails.getLastHeartbeat());
        }
    }

    public void updateSensorReading(SensorType sensorType, SensorReading reading) {
        deviceDetails.getSensors()
                .put(sensorType, reading);
        logger.info("Updated sensor reading of {} to {}", deviceDetails.getDeviceId(), deviceDetails.getSensors()
                .get(sensorType));
        parentObserver.update();
    }

    public String getDeviceId() {
        return deviceDetails.getDeviceId();
    }

    @Override
    public String toString() {
        return deviceDetails.toString();
    }

    @Override
    public void update() {
        if (deviceDetails.getStatus() == DeviceStatus.OFFLINE) {
            deviceDetails.setStatus(DeviceStatus.ONLINE);
        } else {
            deviceDetails.setStatus(DeviceStatus.OFFLINE);
        }

        if (Objects.nonNull(parentObserver)) {
            parentObserver.update();
        }
    }
}
