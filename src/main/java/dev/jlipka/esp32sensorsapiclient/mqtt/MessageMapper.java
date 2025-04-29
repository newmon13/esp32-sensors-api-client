package dev.jlipka.esp32sensorsapiclient.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.EspTopic;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.HeartbeatMessage;
import dev.jlipka.esp32sensorsapiclient.mqtt.discovery.DiscoveryMessage;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorReading;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    private final ObjectMapper objectMapper;

    public MessageMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DiscoveryMessage toDiscoveryMessage(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), DiscoveryMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public HeartbeatMessage toHeartbeatMessage(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), HeartbeatMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public SensorReading toSensorReading(Message<?> message) {
        try {
            return objectMapper.readValue((String) message.getPayload(), SensorReading.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public static EspTopic extractEspTopic(String receivedTopic) {
        if (receivedTopic.equals("system/discovery")) {
            return EspTopic.DISCOVERY;
        } else if (receivedTopic.matches("devices/.+/heartbeat")) {
            return EspTopic.HEARTBEAT;
        } else if (receivedTopic.matches("devices/.+/sensors/.+")) {
            return EspTopic.SENSOR;
        } else {
            return EspTopic.UNKNOWN;
        }
    }

    public static String extractDeviceIdFromTopic(String topic) {
        String[] parts = topic.split("/");
        return parts.length >= 2 ? parts[1] : "unknown";
    }

    public static SensorType extractSensorTypeFromTopic(String topic) {
        String[] parts = topic.split("/");
        return parts.length >= 4 ? SensorType.valueOf(parts[3].toUpperCase()) : SensorType.UNKNOWN;
    }
}
