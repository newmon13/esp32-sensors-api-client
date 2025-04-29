package dev.jlipka.esp32sensorsapiclient.sensor;


public record SensorReading(long rawValue, float normalizedValue, Severity severity) {

}
