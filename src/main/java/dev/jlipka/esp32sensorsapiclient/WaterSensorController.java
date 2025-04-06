package dev.jlipka.esp32sensorsapiclient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WaterSensorController {
    private final WaterLevelSensorService waterLevelSensorService;

    public WaterSensorController(WaterLevelSensorService waterLevelSensorService) {
        this.waterLevelSensorService = waterLevelSensorService;
    }

    @GetMapping("/water-sensor")
    public ResponseEntity<Map> getWaterLevel() {
        return ResponseEntity.ok(waterLevelSensorService.getWaterLevel());
    }

}
