package com.centroweg.iot.time_trial_api.inbound.mqtt;

import com.centroweg.iot.time_trial_api.config.MqttProperties;
import com.centroweg.iot.time_trial_api.core.event.CarroPassouNoSensorEvent;
import com.centroweg.iot.time_trial_api.inbound.dto.SensorPayloadDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttReceiver implements MqttCallback {

    private final MqttClient mqttClient;
    private final MqttProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(this);
            mqttClient.subscribe(properties.getTopic(), 0);
            log.info("Escutando o tópico MQTT: {}", properties.getTopic());
        } catch (Exception e) {
            log.error("Erro ao se inscrever no tópico MQTT", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Conexão MQTT perdida! O Paho vai tentar reconectar automaticamente.", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

        String payload = new String(message.getPayload());
        log.info("JSON recebido no tópico [{}]: {}", topic, payload);

        try {
            SensorPayloadDTO dto = objectMapper.readValue(payload, SensorPayloadDTO.class);

            eventPublisher.publishEvent(new CarroPassouNoSensorEvent(dto.rfid(), dto.timestampMs()));

        } catch (Exception e) {
            log.error("Erro ao processar o payload MQTT: {}", payload, e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
