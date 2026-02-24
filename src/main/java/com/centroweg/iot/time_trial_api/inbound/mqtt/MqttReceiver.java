package com.centroweg.iot.time_trial_api.inbound.mqtt;

import com.centroweg.iot.time_trial_api.config.MqttProperties;
import com.centroweg.iot.time_trial_api.core.event.CarroPassouNoSensorEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttReceiver {

    private final MqttClient mqttClient;
    private final MqttProperties properties;
    private final ApplicationEventPublisher publisher;

    @PostConstruct
    public void subscribe() {
        try {

            mqttClient.subscribe(properties.getTopic(), this::handleMessage);

            log.info("MQTT subscribed to topic: {}", properties.getTopic());

        } catch (MqttException e) {
            log.error("Erro ao se inscrever no tópico MQTT", e);
        }
    }

    private void handleMessage(String topic, MqttMessage message) {

        try {

            String payload = new String(message.getPayload()).trim();

            log.info("Mensagem recebida do MQTT [{}]: {}", topic, payload);

            long timestamp = System.currentTimeMillis();

            publisher.publishEvent(
                    new CarroPassouNoSensorEvent(payload, timestamp)
            );

        } catch (Exception e) {
            log.error("Erro ao processar mensagem MQTT", e);
        }
    }
}
