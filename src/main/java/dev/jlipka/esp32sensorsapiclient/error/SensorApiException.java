package dev.jlipka.esp32sensorsapiclient.error;

public class SensorApiException extends RuntimeException {
    public SensorApiException(String message) {
        super(message);
    }
}
