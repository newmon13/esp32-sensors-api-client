package dev.jlipka.esp32sensorsapiclient.watersensor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WaterSensorReading(@JsonProperty("water_level") Integer waterLevel) {
}