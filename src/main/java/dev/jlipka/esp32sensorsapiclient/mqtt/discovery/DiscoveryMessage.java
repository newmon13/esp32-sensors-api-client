package dev.jlipka.esp32sensorsapiclient.mqtt.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;

import java.util.List;

public record DiscoveryMessage(
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("ip_address") String ipAddress,
        @JsonProperty("sensors") List<SensorType> sensors
) {
}
