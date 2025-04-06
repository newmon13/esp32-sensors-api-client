package dev.jlipka.esp32sensorsapiclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class Esp32ApiClient {
    @Value("${esp32.url}")
    private String ESP32_URL;

    private final RestClient restClient;

    public Esp32ApiClient() {
        this.restClient = RestClient.create();
    }

    public Map getWaterLevel() {
        return restClient.get()
                .uri(ESP32_URL + "/api/data")
                .retrieve()
                .toEntity(Map.class).getBody();
    }

}
