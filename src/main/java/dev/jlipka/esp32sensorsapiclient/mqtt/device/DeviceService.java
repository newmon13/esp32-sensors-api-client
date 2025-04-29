package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import dev.jlipka.esp32sensorsapiclient.mqtt.MessageMapper;
import dev.jlipka.esp32sensorsapiclient.mqtt.PendingMessageService;
import dev.jlipka.esp32sensorsapiclient.mqtt.discovery.DiscoveryMessage;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorReading;
import dev.jlipka.esp32sensorsapiclient.sensor.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static dev.jlipka.esp32sensorsapiclient.mqtt.MessageMapper.extractDeviceIdFromTopic;
import static dev.jlipka.esp32sensorsapiclient.mqtt.MessageMapper.extractEspTopic;
import static dev.jlipka.esp32sensorsapiclient.mqtt.MessageMapper.extractSensorTypeFromTopic;

@Service
public class DeviceService implements Observable, Observer {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private static final String[] BASE_TOPICS = new String[]{"heartbeat"};

    private final IntegrationFlowContext ctx;

    private final Map<String, Device> devices;

    private final MessageMapper messageMapper;

    private final List<Observer> observers;

    private final PendingMessageService pendingMessageService;

    public DeviceService(IntegrationFlowContext ctx, MessageMapper messageMapper, PendingMessageService pendingMessageService) {
        this.ctx = ctx;
        this.messageMapper = messageMapper;
        this.pendingMessageService = pendingMessageService;
        this.devices = new HashMap<>();
        this.observers = new ArrayList<>();
    }

    public void register(DiscoveryMessage discoveryMessage) {

        if (deviceAlreadyRegistered(discoveryMessage.deviceId())) {
            logger.info("Device already registered: {}", discoveryMessage);
        } else {
            logger.info("Started registration of device: {}", discoveryMessage);

            Device newDevice = Device.from(discoveryMessage);
            newDevice.setParentObserver(this);
            List<String> topics = getDeviceTopics(discoveryMessage);

            for (String topic : topics) {
                String specificTopic = "devices/" + discoveryMessage.deviceId() + "/" + topic;
                registerMessageHandler(specificTopic, newDevice);
            }
            devices.put(newDevice.getDeviceId(), newDevice);
            pendingMessageService.process();
            notifyObservers();
        }
    }

    private boolean deviceAlreadyRegistered(String deviceId) {
        return devices.keySet()
                .stream()
                .anyMatch(d -> d.equals(deviceId));
    }

    private List<String> getDeviceTopics(DiscoveryMessage discoveryMessage) {
        List<String> topics;
        if (discoveryMessage.sensors() == null || discoveryMessage.sensors()
                .isEmpty()) {
            topics = Arrays.asList(BASE_TOPICS);
        } else {
            topics = Stream.concat(Arrays.stream(BASE_TOPICS), discoveryMessage.sensors()
                            .stream()
                            .map(sensorType -> "sensors/" + sensorType.name()
                                    .toLowerCase()))
                    .toList();
        }
        return topics;
    }

    public void registerMessageHandler(String specificTopic, Device device) {
        String flowId = specificTopic.replace("/", "_") + "Flow";

        EspTopic espTopic = extractEspTopic(specificTopic);
        String deviceId = extractDeviceIdFromTopic(specificTopic);

        IntegrationFlow flow = IntegrationFlow.from(espTopic.toString()
                        .toLowerCase() + "Channel")
                .filter(deviceTopicSelector(deviceId, espTopic))
                .handle(getMessageHandler(specificTopic, device))
                .get();

        ctx.registration(flow)
                .id(flowId)
                .register();
    }

    private MessageHandler getMessageHandler(String topic, Device device) {
        EspTopic espTopic = extractEspTopic(topic);
        return switch (espTopic) {
            case HEARTBEAT -> message -> {
                HeartbeatMessage heartbeatMessage = messageMapper.toHeartbeatMessage(message);
                device.updateHeartbeat(heartbeatMessage);
            };
            case SENSOR -> message -> {
                logger.error("SENSOR DATA CAME ");
                SensorType sensorType = extractSensorTypeFromTopic(topic);
                SensorReading sensorReading = messageMapper.toSensorReading(message);
                device.updateSensorReading(sensorType, sensorReading);
                notifyObservers();
            };
            default -> message -> {
                logger.warn("Received message on unknown topic for device {}. Payload: {}", device.toString(), message.getPayload());
            };
        };
    }

    public MessageSelector deviceTopicSelector(String deviceId, EspTopic espTopic) {
        return message -> {
            String topic = message.getHeaders()
                    .get(MqttHeaders.RECEIVED_TOPIC, String.class);
            if (topic == null) {
                return false;
            }
            boolean correctDeviceId = extractDeviceIdFromTopic(topic).equals(deviceId);
            boolean correctTopicType = extractEspTopic(topic) == espTopic;
            return correctDeviceId && correctTopicType;
        };
    }

    public Map<String, Device> getDevices() {
        return devices;
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

    public void notifyObservers() {
        logger.debug("Notifying {} observers", observers.size());
        observers.forEach(Observer::update);
    }

    @Override
    public void update() {
        notifyObservers();
    }
}