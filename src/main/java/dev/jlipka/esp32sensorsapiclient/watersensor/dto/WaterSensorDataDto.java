package dev.jlipka.esp32sensorsapiclient.watersensor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WaterSensorDataDto(@JsonProperty("water_level") Integer waterLevel) {
}