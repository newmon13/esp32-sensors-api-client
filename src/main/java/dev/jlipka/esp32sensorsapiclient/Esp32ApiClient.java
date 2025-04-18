package dev.jlipka.esp32sensorsapiclient;

import dev.jlipka.esp32sensorsapiclient.smokesensor.SmokeSensorDataDto;
import dev.jlipka.esp32sensorsapiclient.watersensor.WaterSensorDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Esp32ApiClient {
    private final RestClient restClient;
    @Value("${esp32.url}")
    private String espUrl;

    public Esp32ApiClient() {
        this.restClient = RestClient.create();
    }

    public WaterSensorDataDto getWaterLevel() {
        return restClient.get()
                .uri(espUrl + "/api/water-sensor")
                .retrieve()
                .body(WaterSensorDataDto.class);
    }

    public SmokeSensorDataDto getSmokeLevel() {
        return restClient.get()
                .uri(espUrl + "/api/smoke-sensor")
                .retrieve()
                .body(SmokeSensorDataDto.class);
    }
}
