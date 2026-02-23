package com.centroweg.iot.time_trial_api.core.domain;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table ("historico_carro")
public class HistoricoCarro {

    @PrimaryKeyColumn(name = "carro_id", type = PrimaryKeyType.PARTITIONED)
    private String carroId;

    @PrimaryKeyColumn(name = "timestamp_ms", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Long timestampMs;
}
