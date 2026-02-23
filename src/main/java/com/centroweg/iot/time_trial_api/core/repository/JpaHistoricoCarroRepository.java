package com.centroweg.iot.time_trial_api.core.repository;

import com.centroweg.iot.time_trial_api.core.domain.HistoricoCarro;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface JpaHistoricoCarroRepository extends CassandraRepository<HistoricoCarro, String> {
    HistoricoCarro findFirstByCarroId(String carroId);
}
