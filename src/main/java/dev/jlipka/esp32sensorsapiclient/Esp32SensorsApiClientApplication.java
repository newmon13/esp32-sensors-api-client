package dev.jlipka.esp32sensorsapiclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Esp32SensorsApiClientApplication {
    @Autowired
    Esp32ApiClient esp32ApiClient;

    public static void main(String[] args) {
        SpringApplication.run(Esp32SensorsApiClientApplication.class, args);
    }


    @EventListener(ApplicationStartedEvent.class)
    public void onStartup() {
        esp32ApiClient.connectToEsp();
    }

}
