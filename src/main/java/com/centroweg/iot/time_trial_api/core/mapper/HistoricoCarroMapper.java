package com.centroweg.iot.time_trial_api.core.mapper;

import com.centroweg.iot.time_trial_api.core.domain.HistoricoCarro;
import com.centroweg.iot.time_trial_api.outbound.dto.HistoricoCarroResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class HistoricoCarroMapper {

    public HistoricoCarroResponseDTO toDto(HistoricoCarro domain) {

        return new HistoricoCarroResponseDTO(
                domain.getCarroId(),
                domain.getTimestampMs()
        );
    }
}
