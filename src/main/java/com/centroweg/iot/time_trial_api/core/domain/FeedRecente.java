package com.centroweg.iot.time_trial_api.core.domain;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("feed_recente")
public class FeedRecente {
    @PrimaryKeyColumn(name = "agrupador", type = PrimaryKeyType.PARTITIONED)
    private String agrupador = "GERAL";

    @PrimaryKeyColumn(name = "timestamp_ms", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Long timestampMs;

    @Column("carro_id")
    private String carroId;

    @Column("tempo_volta_ms")
    private Long tempoVoltaMs;
}