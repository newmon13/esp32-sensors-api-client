package dev.jlipka.esp32sensorsapiclient.mqtt.discovery;

import dev.jlipka.esp32sensorsapiclient.mqtt.device.DeviceService;
import dev.jlipka.esp32sensorsapiclient.mqtt.device.NewDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryService.class);

    private final MessageMapper messageMapper;
    private final DeviceService deviceService;

    public DiscoveryService(DeviceService deviceService, MessageMapper messageMapper) {
        this.deviceService = deviceService;
        this.messageMapper = messageMapper;

    }

    public void discover(Message<?> message) {
        NewDevice newDevice = messageMapper.toNewDevice(message);
        logger.info("Discovered device: {}", newDevice);

        deviceService.register(newDevice);
    }
}