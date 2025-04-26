package dev.jlipka.esp32sensorsapiclient.smokesensor;

import dev.jlipka.esp32sensorsapiclient.Esp32ApiClient;
import dev.jlipka.esp32sensorsapiclient.error.SensorApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SmokeSensorService {
    private final static String SMOKE_SENSOR_CB_NAME = "smokeSensorCb";
    private final static String SMOKE_SENSOR_TL_NAME = "smokeSensorTl";

    private final Esp32ApiClient esp32ApiClient;

    public SmokeSensorService(Esp32ApiClient esp32ApiClient) {
        this.esp32ApiClient = esp32ApiClient;
    }

    @CircuitBreaker(name = SMOKE_SENSOR_CB_NAME, fallbackMethod = "getSmokeLevelFallback")
    @TimeLimiter(name = SMOKE_SENSOR_TL_NAME, fallbackMethod = "getSmokeLevelFallback")
    public CompletableFuture<SmokeSensorReading> getSmokeLevel() {
        return CompletableFuture.supplyAsync(esp32ApiClient::getSmokeLevel);
    }

    private CompletableFuture<SmokeSensorReading> getSmokeLevelFallback(Throwable throwable) {
        throw new SensorApiException("Lost connection to smoke sensor API");
    }
}
