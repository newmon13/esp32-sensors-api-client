package dev.jlipka.esp32sensorsapiclient.mqtt;

import dev.jlipka.esp32sensorsapiclient.mqtt.device.EspTopic;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

@Service
public class PendingMessageService {

    private final Queue<Message<?>> pendingMessages = new LinkedList<>();

    private final MessageChannel heartbeatChannel;

    private final MessageChannel sensorChannel;

    public PendingMessageService(@Lazy MessageChannel heartbeatChannel, @Lazy MessageChannel sensorChannel) {
        this.heartbeatChannel = heartbeatChannel;
        this.sensorChannel = sensorChannel;
    }

    public void add(Message<?> message) {
        pendingMessages.add(message);
    }

    public void process() {
        while (!pendingMessages.isEmpty()) {

            Message<?> message = pendingMessages.poll();
            String receivedTopic = message.getHeaders()
                    .get(MqttHeaders.RECEIVED_TOPIC, String.class);
            EspTopic topic = MessageMapper.extractEspTopic(Objects.requireNonNull(receivedTopic));
            switch (topic) {
                case HEARTBEAT -> heartbeatChannel.send(message);
                case SENSOR -> sensorChannel.send(message);
            }
        }
    }
}
