package com.centroweg.iot.time_trial_api.core.domain;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("podio_global")
public class PodioGlobal {

    @PrimaryKeyColumn(name = "agrupador", type = PrimaryKeyType.PARTITIONED)
    private String agrupador = "GERAL";

    @PrimaryKeyColumn(name = "tempo_volta_ms", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private Long tempoVoltaMs;

    @PrimaryKeyColumn(name = "carro_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private String carroId;
}
