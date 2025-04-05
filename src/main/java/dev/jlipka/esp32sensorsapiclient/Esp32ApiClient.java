package dev.jlipka.esp32sensorsapiclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Esp32ApiClient {
    @Value("${esp32.url}")
    private String ESP32_URL;

    private final RestClient restClient;

    public Esp32ApiClient() {
        this.restClient = RestClient.create();
    }

    public String connectToEsp() {
        ResponseEntity<String> entity = restClient.get()
                .uri(ESP32_URL)
                .retrieve()
                .toEntity(String.class);
        System.out.println(entity.getBody().toString());
        return entity.getBody().toString();
    }

}
