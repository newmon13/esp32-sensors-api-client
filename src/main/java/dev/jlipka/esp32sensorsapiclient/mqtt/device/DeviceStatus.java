package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import java.time.Instant;

public record DeviceStatus(Long deviceId, String status, Instant timestamp) {
}
