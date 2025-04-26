package dev.jlipka.esp32sensorsapiclient.mqtt.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static dev.jlipka.esp32sensorsapiclient.mqtt.discovery.MessageMapper.extractDeviceIdFromTopic;
import static dev.jlipka.esp32sensorsapiclient.mqtt.discovery.MessageMapper.extractEspTopic;

@Service
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private static final String[] BASE_TOPICS = new String[]{"status", "heartbeat"};
    private final IntegrationFlowContext ctx;

    private final Set<NewDevice> devices;

    public DeviceService(IntegrationFlowContext ctx) {
        this.ctx = ctx;
        this.devices = new HashSet<>();
    }

    public void register(NewDevice newDevice) {
        boolean deviceExists = devices.stream()
                .anyMatch(device -> device.deviceId().equals(newDevice.deviceId()));
        if (!deviceExists) {
            List<String> topics;
            if (newDevice.sensors() == null) {
                topics = Arrays.asList(BASE_TOPICS);
            } else {
                topics = Stream.concat(Arrays.stream(BASE_TOPICS), newDevice.sensors()
                                .stream())
                        .toList();
            }

            logger.info("Started registration of device: {}", newDevice);

            for (String topic : topics) {
                String specificTopic = "devices/" + newDevice.deviceId() + "/" + topic;
                addTopicMessageHandler(specificTopic);
            }
        } else {
            logger.error("Device already registered");
        }
    }

    public void addTopicMessageHandler(String specificTopic) {
        String flowId = specificTopic.replace("/", "_") + "Flow";

        EspTopic espTopic = extractEspTopic(specificTopic);
        String deviceId = extractDeviceIdFromTopic(specificTopic);

        IntegrationFlow flow = IntegrationFlow.from(espTopic.toString()
                        .toLowerCase() + "Channel")
                .filter(deviceTopicSelector(deviceId))
                .handle((Message<?> message) -> {
                    System.out.println("Received message on topic: " + specificTopic);
                    System.out.println("Payload: " + message.getPayload());
                })
                .get();

        ctx.registration(flow)
                .id(flowId)
                .register();

    }

    public MessageSelector deviceTopicSelector(String deviceId) {
        return message -> {
            String topic = message.getHeaders()
                    .get(MqttHeaders.RECEIVED_TOPIC, String.class);
            return topic != null && extractDeviceIdFromTopic(topic).equals(deviceId);
        };
    }
}