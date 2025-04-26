package dev.jlipka.esp32sensorsapiclient.watersensor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WaterSensorController {
    private final WaterSensorService waterSensorService;

    public WaterSensorController(WaterSensorService waterSensorService) {
        this.waterSensorService = waterSensorService;
    }

    @GetMapping("/water-sensor")
    public WaterSensorReading getWaterLevel() {
        return waterSensorService.getWaterLevel()
                .join();
    }
}