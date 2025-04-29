package dev.jlipka.esp32sensorsapiclient.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.DeviceService;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.Observer;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorReading;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;

@Route(value = "/water-detector")
@CssImport("./styles/water-detector.css")
public class WaterSensorView extends VerticalLayout implements HasUrlParameter<String>, Observer {

    private final DeviceService deviceService;
    private Div waterTank;
    private Div water;
    private Span levelLabel;
    private String deviceId;
    private UI ui;

    //TODO this view is not finished

    public WaterSensorView(DeviceService deviceService) {
        this.deviceService = deviceService;
        addClassName("water-detector-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(createTitle(), createWaterTank());
    }

    private Component createTitle() {
        Div title = new Div();
        title.setText("Water Detector");
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

    private void updateWaterLevel() {
        SensorReading sensorReading = deviceService.getDevices()
                .get(deviceId)
                .getDeviceDetails()
                .getSensors()
                .get(SensorType.WATER);

        updateUI(sensorReading.normalizedValue());
    }

    private void updateUI(float waterLevel) {
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
        WaterSensorUpdateThread smokeSensorUpdateThread = new WaterSensorUpdateThread(this.ui, this);
        smokeSensorUpdateThread.start();
    }

    private static class WaterSensorUpdateThread extends Thread {
        private final UI ui;
        private final WaterSensorView view;

        public WaterSensorUpdateThread(UI ui, WaterSensorView view) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            ui.access(view::updateWaterLevel);
        }
    }
}