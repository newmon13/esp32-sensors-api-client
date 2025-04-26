package dev.jlipka.esp32sensorsapiclient.smokesensor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmokeSensorController {

    private final SmokeSensorService smokeSensorService;

    public SmokeSensorController(SmokeSensorService smokeSensorService) {
        this.smokeSensorService = smokeSensorService;
    }

    @GetMapping("/smoke-sensor")
    public SmokeSensorReading getSmokeLevel() {
        return smokeSensorService.getSmokeLevel()
                .join();
    }
}
