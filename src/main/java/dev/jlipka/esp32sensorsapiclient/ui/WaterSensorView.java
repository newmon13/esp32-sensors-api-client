package dev.jlipka.esp32sensorsapiclient.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import dev.jlipka.esp32sensorsapiclient.watersensor.WaterSensorReading;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@Route("/water-detector")
@CssImport("./styles/water-detector.css")
public class WaterSensorView extends VerticalLayout {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    @Value("${sensors.api.url}")
    private String sensorsApiUrl;
    private Div waterTank;
    private Div water;
    private Span levelLabel;

    public WaterSensorView(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();

        addClassName("water-detector-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(createTitle(), createWaterTank(), createControls());
    }

    @PostConstruct
    private void init() {
        updateWaterLevel();
    }

    private Component createTitle() {
        Div title = new Div();
        title.setText("Water Tank");
        title.addClassName("tank-title");
        return title;
    }

    private Component createWaterTank() {
        waterTank = new Div();
        waterTank.addClassName("water-tank");

        water = new Div();
        water.addClassName("water");

        levelLabel = new Span("0%");
        levelLabel.addClassName("level-label");

        waterTank.add(water, levelLabel);

        return waterTank;
    }

    private Component createControls() {
        Div controls = new Div();
        controls.addClassName("controls");

        Button buttonRefreshWaterLevel = new Button("Refresh", e -> {
            updateWaterLevel();
        });

        controls.add(buttonRefreshWaterLevel);
        return controls;
    }

    private void updateWaterLevel() {
        WaterSensorReading waterSensorReading = restClient.get()
                .uri(sensorsApiUrl + "/water-sensor")
                .exchange((request, response) -> {
                    if (response.getStatusCode()
                            .is5xxServerError()) {
                        showNotification("Lost connection to water sensor API", NotificationVariant.LUMO_CONTRAST);
                        return new WaterSensorReading(-1);
                    }
                    return objectMapper.readValue(response.getBody(), new TypeReference<>() {
                    });
                });

        updateUI(waterSensorReading.waterLevel());
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
    }

    private void updateUI(Integer waterLevel) {
        water.getStyle()
                .set("height", waterLevel + "%");
        levelLabel.setText(waterLevel + "%");

        waterTank.removeClassNames("level-low", "level-medium", "level-high", "level-critical");

        if (waterLevel < 20) {
            waterTank.addClassName("level-low");
        } else if (waterLevel < 50) {
            waterTank.addClassName("level-medium");
        } else if (waterLevel < 80) {
            waterTank.addClassName("level-high");
        } else {
            waterTank.addClassName("level-critical");
        }
    }
}