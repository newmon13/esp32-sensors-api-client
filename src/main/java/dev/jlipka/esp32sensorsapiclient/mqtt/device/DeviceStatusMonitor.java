package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceStatusMonitor implements Observable {

    private static final Logger logger = LoggerFactory.getLogger(DeviceStatusMonitor.class);
    private static final int TIMEOUT_SECONDS = 20;

    private final HeartbeatMonitor heartbeatMonitor;
    private final AtomicReference<DeviceStatus> latestStatus = new AtomicReference<>(DeviceStatus.OFFLINE);
    private Thread currentVirtualMonitorThread;
    private final List<Observer> observers;

    public DeviceStatusMonitor() {
        heartbeatMonitor = new HeartbeatMonitor();
        observers = new ArrayList<>();
    }

    private void updateStatus(DeviceStatus status) {
        DeviceStatus last = this.latestStatus.get();
        boolean updated = status != null && !status.equals(last) && this.latestStatus.compareAndSet(last, status);

        if (updated) {
            if (status == DeviceStatus.OFFLINE) {
                logger.warn("Connection to device lost!");
            } else {
                logger.debug("Device status changed from {} to {}", last, status);
            }
            notifyObservers();
        }
    }

    public boolean updateHeartbeat(Instant timestamp) {
        boolean updated = heartbeatMonitor.update(timestamp);

        if (currentVirtualMonitorThread != null && currentVirtualMonitorThread.isAlive()) {
            currentVirtualMonitorThread.interrupt();
            logger.debug("Interrupted previous heartbeat monitor thread");
        }

        updateStatus(DeviceStatus.ONLINE);

        currentVirtualMonitorThread = Thread.startVirtualThread(() -> {
            try {
                logger.debug("Starting heartbeat timeout monitor for {} seconds", TIMEOUT_SECONDS);
                LocalTime endTime = LocalTime.now().plusSeconds(TIMEOUT_SECONDS);
                LocalTime currentTime;

                do {
                    Thread.sleep(1000);
                    currentTime = LocalTime.now();
                } while (currentTime.isBefore(endTime));

                logger.warn("No heartbeat received for {} seconds", TIMEOUT_SECONDS);
                updateStatus(DeviceStatus.OFFLINE);

            } catch (InterruptedException e) {
                logger.debug("Heartbeat monitor thread interrupted - new heartbeat received");
            }
        });
        return updated;
    }

    @Override
    public void addObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        logger.debug("Notifying {} observers about status change", observers.size());
        observers.forEach(Observer::update);
    }
}