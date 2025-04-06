package dev.jlipka.esp32sensorsapiclient;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WaterLevelSensorService {

    private final Esp32ApiClient esp32ApiClient;

    public WaterLevelSensorService(Esp32ApiClient esp32ApiClient) {
        this.esp32ApiClient = esp32ApiClient;
    }


    public Map<String, String> getWaterLevel() {
        return esp32ApiClient.getWaterLevel();
    }
}
