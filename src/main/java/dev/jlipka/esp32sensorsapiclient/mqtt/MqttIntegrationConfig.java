package dev.jlipka.esp32sensorsapiclient.mqtt;

import dev.jlipka.esp32sensorsapiclient.mqtt.device.EspTopic;
import dev.jlipka.esp32sensorsapiclient.mqtt.discovery.DiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static dev.jlipka.esp32sensorsapiclient.mqtt.MessageMapper.extractEspTopic;


@Configuration
@EnableIntegration
public class MqttIntegrationConfig {

    private static final String[] TOPICS = new String[]{"system/discovery", "devices/+/heartbeat", "devices/+/sensors/#"};
    private static final Logger log = LoggerFactory.getLogger(MqttIntegrationConfig.class);

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Autowired
    private DiscoveryService discoveryService;

    @Autowired
    private PendingMessageService pendingMessageService;


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
                        .channelMapping(EspTopic.HEARTBEAT, heartbeatChannel())
                        .channelMapping(EspTopic.SENSOR, sensorChannel())
                        .defaultOutputChannel(pendingChannel())
                        .resolutionRequired(false))
                .get();
    }

    @Bean
    public IntegrationFlow discoveryFlow() {
        return IntegrationFlow.from(discoveryChannel())
                .handle(discoveryService::discover)
                .get();
    }

    @Bean
    public MessageChannel pendingChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow heartbeatToPendingFlow() {
        return IntegrationFlow.from(heartbeatChannel())
                .channel(pendingChannel())
                .get();
    }

    @Bean
    public IntegrationFlow sensorToPendingFlow() {
        return IntegrationFlow.from(sensorChannel())
                .channel(pendingChannel())
                .get();
    }

    @Bean
    public IntegrationFlow pendingFlow() {
        return IntegrationFlow.from(pendingChannel())
                .handle(pendingMessageService::add)
                .get();
    }
}