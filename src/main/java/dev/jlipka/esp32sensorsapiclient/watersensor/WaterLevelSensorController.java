package dev.jlipka.esp32sensorsapiclient.watersensor;

import dev.jlipka.esp32sensorsapiclient.watersensor.dto.WaterSensorDataDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
public class WaterLevelSensorController {
    private final WaterLevelSensorService waterLevelSensorService;

    public WaterLevelSensorController(WaterLevelSensorService waterLevelSensorService) {
        this.waterLevelSensorService = waterLevelSensorService;
    }

    @GetMapping("/water-sensor")
    public ResponseEntity<WaterSensorDataDto> getWaterLevel() throws ExecutionException, InterruptedException {
        WaterSensorDataDto waterSensorDataDto = waterLevelSensorService.getWaterLevel().get();
        return ResponseEntity.ok(waterSensorDataDto);
    }
}