package dev.jlipka.esp32sensorsapiclient.mqtt.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.DeviceStatus;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.EspTopic;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.Heartbeat;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.NewDevice;
import dev.jlipka.esp32sensorsapiclient.smokesensor.SmokeSensorReading;
import dev.jlipka.esp32sensorsapiclient.watersensor.WaterSensorReading;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    private final ObjectMapper objectMapper;

    public MessageMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NewDevice toNewDevice(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), NewDevice.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public DeviceStatus toDeviceStatus(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), DeviceStatus.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Heartbeat toHeartbeat(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), Heartbeat.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public SmokeSensorReading toSmokeSensorReading(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), SmokeSensorReading.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public WaterSensorReading toWaterSensorReading(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), WaterSensorReading.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public static EspTopic extractEspTopic(String receivedTopic) {
        if (receivedTopic.equals("system/discovery")) {
            return EspTopic.DISCOVERY;
        } else if (receivedTopic.matches("devices/.+/status")) {
            return EspTopic.STATUS;
        } else if (receivedTopic.matches("devices/.+/heartbeat")) {
            return EspTopic.HEARTBEAT;
        } else if (receivedTopic.matches("devices/.+/.+")) {
            return EspTopic.SENSOR;
        } else {
            return EspTopic.UNKNOWN;
        }
    }

    public static String extractDeviceIdFromTopic(String topic) {
        String[] parts = topic.split("/");
        return parts.length >= 2 ? parts[1] : "unknown";
    }

    public static String extractSensorTypeFromTopic(String topic) {
        String[] parts = topic.split("/");
        return parts.length >= 4 ? parts[3] : "unknown";
    }
}
