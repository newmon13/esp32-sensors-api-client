package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NewDevice(
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("ip_address") String ipAddress,
        @JsonProperty("sensors") List<String> sensors
) {
}
