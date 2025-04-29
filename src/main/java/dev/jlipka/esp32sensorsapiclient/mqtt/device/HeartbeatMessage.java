package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import java.time.Instant;

public record HeartbeatMessage(Instant timestamp) {
}
