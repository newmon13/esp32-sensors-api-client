package dev.jlipka.esp32sensorsapiclient.watersensor;

import dev.jlipka.esp32sensorsapiclient.watersensor.dto.WaterSensorDataDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WaterLevelSensorController {
    private final WaterLevelSensorService waterLevelSensorService;

    public WaterLevelSensorController(WaterLevelSensorService waterLevelSensorService) {
        this.waterLevelSensorService = waterLevelSensorService;
    }

    @GetMapping("/water-sensor")
    public WaterSensorDataDto getWaterLevel() {
        return waterLevelSensorService.getWaterLevel()
                .join();
    }
}