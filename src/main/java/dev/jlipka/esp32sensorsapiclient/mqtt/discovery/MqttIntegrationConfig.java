package dev.jlipka.esp32sensorsapiclient.mqtt.discovery;

import dev.jlipka.esp32sensorsapiclient.mqtt.device.EspTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import java.util.Objects;

import static dev.jlipka.esp32sensorsapiclient.mqtt.discovery.MessageMapper.extractEspTopic;


@Configuration
@EnableIntegration
public class MqttIntegrationConfig {

    private static final String[] TOPICS = new String[]{"system/discovery", "devices/+/status", "devices/+/heartbeat", "devices/+/sensor/#"};

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Autowired
    private DiscoveryService discoveryService;


    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(brokerUrl, "testClient", TOPICS);
        adapter.setOutputChannel(mqttInputChannel());
        adapter.setQos(1);
        adapter.setCompletionTimeout(5000);
        return adapter;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel discoveryChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel statusChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel heartbeatChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel sensorChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow routerFlow() {
        return IntegrationFlow.from(mqttInboundAdapter())
                .route(Message.class, message -> {

                    MessageHeaders headers = message.getHeaders();
                    String receivedTopic = headers.get(MqttHeaders.RECEIVED_TOPIC, String.class);
                    return extractEspTopic(Objects.requireNonNull(receivedTopic));

                }, mapping -> mapping.channelMapping(EspTopic.DISCOVERY, discoveryChannel())
                        .channelMapping(EspTopic.STATUS, statusChannel())
                        .channelMapping(EspTopic.HEARTBEAT, heartbeatChannel())
                        .channelMapping(EspTopic.SENSOR, sensorChannel()))
                .get();
    }

    @Bean
    public IntegrationFlow discoveryFlow() {
        return IntegrationFlow.from(discoveryChannel())
                .handle(discoveryService::discover)
                .get();
    }

//
//    @Bean
//    public IntegrationFlow statusFlow() {
//        return IntegrationFlow.from(statusChannel())
//                .handle((payload, headers) -> {
//                    String topic = headers.get(MqttHeaders.RECEIVED_TOPIC, String.class);
//                    String deviceId = extractDeviceIdFromTopic(Objects.requireNonNull(topic));
//                    System.out.println("Device status " + deviceId + ": " + payload);
//                    return null;
//                })
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow heartbeatFlow() {
//        return IntegrationFlow.from(heartbeatChannel())
//                .handle((payload, headers) -> {
//                    String topic = headers.get(MqttHeaders.RECEIVED_TOPIC, String.class);
//                    String deviceId = extractDeviceIdFromTopic(Objects.requireNonNull(topic));
//                    System.out.println("Device heartbeat " + deviceId);
//                    return null;
//                })
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow sensorFlow() {
//        return IntegrationFlow.from(sensorChannel())
//                .handle((payload, headers) -> {
//                    String topic = headers.get(MqttHeaders.RECEIVED_TOPIC, String.class);
//                    String deviceId = extractDeviceIdFromTopic(Objects.requireNonNull(topic));
//                    String sensorType = extractSensorTypeFromTopic(topic);
//                    System.out.println("Data from sensor " + sensorType + " of device " + deviceId + ": " + payload);
//
//                    return null;
//                })
//                .get();
//    }
}