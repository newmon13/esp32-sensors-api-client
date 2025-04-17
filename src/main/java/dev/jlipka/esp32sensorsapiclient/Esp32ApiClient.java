package dev.jlipka.esp32sensorsapiclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class Esp32ApiClient {
    @Value("${esp32.url}")
    private String espUrl;

    private final RestClient restClient;

    public Esp32ApiClient() {
        this.restClient = RestClient.create();
    }

    public Map<String, Integer> getWaterLevel() {
        return restClient.get()
                .uri(espUrl + "/api/water-sensor")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
