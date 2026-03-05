package com.centroweg.iot.time_trial_api.mqtt;

import com.centroweg.iot.time_trial_api.dto.LapTimeMessage;
import com.centroweg.iot.time_trial_api.service.LapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttMessageHandler implements IMqttMessageListener {

    private final LapService lapService;
    private final ObjectMapper objectMapper;

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.info("MQTT message received on topic '{}': {}", topic, payload);

            LapTimeMessage lapTimeMessage = objectMapper.readValue(payload, LapTimeMessage.class);
            lapService.recordLap(lapTimeMessage);

            log.info("Lap recorded: raceId={}, carId={}, lap={}, time={}ms",
                    lapTimeMessage.getRaceId(),
                    lapTimeMessage.getCarId(),
                    lapTimeMessage.getLapNumber(),
                    lapTimeMessage.getTimeMillis());
        } catch (Exception e) {
            log.error("Failed to process MQTT message on topic '{}': {}", topic, e.getMessage(), e);
        }
    }
}
