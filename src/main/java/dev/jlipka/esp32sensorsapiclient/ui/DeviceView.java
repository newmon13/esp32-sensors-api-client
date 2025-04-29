package dev.jlipka.esp32sensorsapiclient.ui;


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.Device;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.DeviceDetails;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.DeviceService;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.Observer;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;

import java.util.Map;

@Route("devices")
public class DeviceView extends VerticalLayout implements Observer {

    private static final Map<SensorType, Class<? extends Component>> sensorViews = Map.of(SensorType.WATER, WaterSensorView.class, SensorType.SMOKE, SmokeSensorView.class);
    private final DeviceService deviceService;
    private final Grid<DeviceDetails> deviceGrid;
    private UI ui;

    public DeviceView(DeviceService deviceService) {
        this.deviceService = deviceService;
        this.deviceGrid = new Grid<>(DeviceDetails.class);
        this.deviceGrid.setColumns("deviceId", "ipAddress", "status", "lastHeartbeat", "sensors");
        add(new H2("Devices"), deviceGrid);
    }

    public void refreshGrid() {
        ListDataProvider<DeviceDetails> deviceMonitorListDataProvider = DataProvider.fromStream(deviceService.getDevices()
                .values()
                .stream()
                .map(Device::getDeviceDetails));

        deviceGrid.setItems(deviceMonitorListDataProvider);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        deviceService.addObserver(this);
        refreshGrid();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        deviceService.removeObserver(this);
        refreshGrid();
    }

    @Override
    public void update() {
        DeviceGridUpdateThread thread = new DeviceGridUpdateThread(ui, this);
        thread.start();
    }

    private static class DeviceGridUpdateThread extends Thread {
        private final UI ui;
        private final DeviceView view;

        public DeviceGridUpdateThread(UI ui, DeviceView view) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            ui.access(view::refreshGrid);
        }
    }
}