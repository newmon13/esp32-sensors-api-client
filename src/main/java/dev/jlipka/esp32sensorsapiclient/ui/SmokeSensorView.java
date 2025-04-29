package dev.jlipka.esp32sensorsapiclient.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.DeviceService;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.Observer;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorReading;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;
import dev.jlipka.esp32sensorsapiclient.sensor.Severity;

import java.util.List;

@Route(value = "/smoke-detector")
@CssImport("./styles/smoke-detector.css")
public class SmokeSensorView extends VerticalLayout implements HasUrlParameter<String>, Observer {

    private final Div smokeIndicator = new Div();
    private final UnorderedList smokeAnimation = new UnorderedList();
    private final DeviceService deviceService;
    private String deviceId;
    private UI ui;

    public SmokeSensorView(DeviceService deviceService) {
        this.deviceService = deviceService;

        addClassName("smoke-detector-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(createTitle(), createSmokeIndicator(), createSmokeAnimation());
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

    public void updateSmokeLevel() {
        SensorReading sensorReading = deviceService.getDevices()
                .get(deviceId)
                .getDeviceDetails()
                .getSensors()
                .get(SensorType.SMOKE);
        updateUI(sensorReading);
    }

    public void updateUI(SensorReading dto) {
        updateSmokeIndicator(dto.severity());
        adjustSmokeAnimation(dto.severity());

        if (dto.severity() == Severity.HIGH) {
            Notification.show("WARNING! HIGH SMOKE LEVEL!", 5000, Notification.Position.BOTTOM_CENTER);
        }
    }

    private void updateSmokeIndicator(Severity severity) {
        Style smokeIndicatorStyle = smokeIndicator.getStyle();
        switch (severity) {
            case NONE -> smokeIndicatorStyle.set("background-color", "white");
            case LOW -> smokeIndicatorStyle.set("background-color", "green");
            case MEDIUM -> smokeIndicatorStyle.set("background-color", "orange");
            case HIGH -> smokeIndicatorStyle.set("background-color", "red");
        }
    }

    private void adjustSmokeAnimation(Severity level) {
        List<Component> list = smokeAnimation.getChildren()
                .toList();

        for (int i = 0; i < list.size(); i++) {
            Component component = list.get(i);
            if (component instanceof ListItem item) {
                switch (level) {
                    case NONE -> item.getStyle()
                            .set("opacity", "0");
                    case LOW -> item.getStyle()
                            .set("width", "15px")
                            .set("height", "15px")
                            .set("animation-name", (i % 2 == 0) ? "animateEvenLow" : "animateOddLow")
                            .set("animation-duration", "4s");
                    case MEDIUM -> item.getStyle()
                            .set("width", "25px")
                            .set("height", "25px")
                            .set("animation-name", (i % 2 == 0) ? "animateEvenMedium" : "animateOddMedium")
                            .set("animation-duration", "3s");
                    case HIGH -> item.getStyle()
                            .set("width", "40px")
                            .set("height", "40px")
                            .set("animation-name", (i % 2 == 0) ? "animateEvenHigh" : "animateOddHigh")
                            .set("animation-duration", "2s");
                }
            }
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        deviceService.addObserver(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        deviceService.removeObserver(this);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void update() {
        SmokeSensorUpdateThread smokeSensorUpdateThread = new SmokeSensorUpdateThread(this.ui, this);
        smokeSensorUpdateThread.start();
    }

    private static class SmokeSensorUpdateThread extends Thread {
        private final UI ui;
        private final SmokeSensorView view;

        public SmokeSensorUpdateThread(UI ui, SmokeSensorView view) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            ui.access(view::updateSmokeLevel);
        }
    }
}