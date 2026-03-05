package com.centroweg.iot.time_trial_api.mqtt;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttSubscriber {

    private final IMqttClient mqttClient;
    private final MqttMessageHandler messageHandler;

    @Value("${mqtt.topics.lap-completed}")
    private String lapCompletedTopic;

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() throws MqttException {
        log.info("Subscribing to MQTT topic: {}", lapCompletedTopic);
        mqttClient.subscribe(lapCompletedTopic, 1, messageHandler);
        log.info("Successfully subscribed to MQTT topic: {}", lapCompletedTopic);
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            log.error("Error disconnecting from MQTT broker", e);
        }
    }
}
