package dev.jlipka.esp32sensorsapiclient.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import dev.jlipka.esp32sensorsapiclient.smokesensor.SmokeLevelSeverity;
import dev.jlipka.esp32sensorsapiclient.smokesensor.SmokeSensorDataDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.util.List;

@Route("/smoke-detector")
@CssImport("./styles/smoke-detector.css")
public class SmokeSensorView extends VerticalLayout {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Div smokeIndicator = new Div();
    private final UnorderedList smokeAnimation = new UnorderedList();
    @Value("${sensors.api.url}")
    private String sensorsApiUrl;

    public SmokeSensorView(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();

        addClassName("smoke-detector-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(createTitle(), createSmokeIndicator(), createSmokeAnimation());
        add(createControls());
    }

    @PostConstruct
    private void init() {
        updateSmokeLevel();
    }

    private Component createTitle() {
        Div title = new Div();
        title.setText("Smoke Detector");
        title.addClassName("smoke-title");
        return title;
    }

    private Component createSmokeIndicator() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(Alignment.CENTER);
        layout.setSpacing(false);
        layout.setPadding(false);

        smokeIndicator.addClassName("smoke-indicator");
        layout.add(smokeIndicator);
        return layout;
    }

    private Component createSmokeAnimation() {
        Div container = new Div();
        container.addClassName("smoke-animation-container");

        for (int i = 0; i < 8; i++) {
            ListItem listItem = new ListItem();
            smokeAnimation.add(listItem);
        }

        container.add(smokeAnimation);
        return container;
    }

    private Component createControls() {
        Div controls = new Div();
        controls.addClassName("controls");

        Button buttonRefreshWaterLevel = new Button("Refresh", e -> {
            updateSmokeLevel();
        });

        controls.add(buttonRefreshWaterLevel);
        return controls;
    }

    private void updateSmokeLevel() {
        SmokeSensorDataDto dto = restClient.get()
                .uri(sensorsApiUrl + "/smoke-sensor")
                .exchange((request, response) -> {
                    if (response.getStatusCode()
                            .is5xxServerError()) {
                        showNotification("Lost connection to smoke sensor API", NotificationVariant.LUMO_CONTRAST);
                        return new SmokeSensorDataDto(-1, false, -1, null);
                    }
                    return objectMapper.readValue(response.getBody(), SmokeSensorDataDto.class);
                });
        updateUI(dto);
    }

    private void updateUI(SmokeSensorDataDto dto) {
        updateSmokeIndicator(dto.severity());
        adjustSmokeAnimation(dto.severity());

        if (dto.severity() == SmokeLevelSeverity.HIGH) {
            showNotification("WARNING! HIGH SMOKE LEVEL!", NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateSmokeIndicator(SmokeLevelSeverity severity) {
        Style smokeIndicatorStyle = smokeIndicator.getStyle();
        switch (severity) {
            case NONE -> smokeIndicatorStyle.set("background-color", "white");
            case LOW -> smokeIndicatorStyle.set("background-color", "green");
            case MEDIUM -> smokeIndicatorStyle.set("background-color", "orange");
            case HIGH -> smokeIndicatorStyle.set("background-color", "red");
        }
    }

    private void adjustSmokeAnimation(SmokeLevelSeverity level) {
        List<Component> list = smokeAnimation.getChildren()
                .toList();

        for (int i = 0; i < list.size(); i++) {
            Component component = list.get(i);
            if (component instanceof ListItem item) {
                switch (level) {
                    case NONE -> {
                        item.getStyle()
                                .set("opacity", "0");
                    }
                    case LOW -> {
                        item.getStyle()
                                .set("width", "15px")
                                .set("height", "15px")
                                .set("animation-name", (i % 2 == 0) ? "animateEvenLow" : "animateOddLow")
                                .set("animation-duration", "4s");
                    }
                    case MEDIUM -> {
                        item.getStyle()
                                .set("width", "25px")
                                .set("height", "25px")
                                .set("animation-name", (i % 2 == 0) ? "animateEvenMedium" : "animateOddMedium")
                                .set("animation-duration", "3s");
                    }
                    case HIGH -> {
                        item.getStyle()
                                .set("width", "40px")
                                .set("height", "40px")
                                .set("animation-name", (i % 2 == 0) ? "animateEvenHigh" : "animateOddHigh")
                                .set("animation-duration", "2s");
                    }
                }
            }
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
    }
}