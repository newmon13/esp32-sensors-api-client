package dev.jlipka.esp32sensorsapiclient.watersensor;

import dev.jlipka.esp32sensorsapiclient.Esp32ApiClient;
import dev.jlipka.esp32sensorsapiclient.error.SensorApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class WaterSensorService {
    private final static String WATER_SENSOR_CB_NAME = "waterSensorCb";
    private final static String WATER_SENSOR_TL_NAME = "waterSensorTl";

    private final Esp32ApiClient esp32ApiClient;

    public WaterSensorService(Esp32ApiClient esp32ApiClient) {
        this.esp32ApiClient = esp32ApiClient;
    }

    @CircuitBreaker(name = WATER_SENSOR_CB_NAME, fallbackMethod = "getWaterLevelFallback")
    @TimeLimiter(name = WATER_SENSOR_TL_NAME, fallbackMethod = "getWaterLevelFallback")
    public CompletableFuture<WaterSensorReading> getWaterLevel() {
        return CompletableFuture.supplyAsync(esp32ApiClient::getWaterLevel);
    }

    private CompletableFuture<WaterSensorReading> getWaterLevelFallback(Throwable throwable) {
        throw new SensorApiException("Lost connection to water sensor API");
    }
}