package com.centroweg.iot.time_trial_api.core.domain;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("pista")
public class Pista {

    @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.PARTITIONED)
    private String id;

    @Column("nome")
    private String nome;

    @Column("tempo_minimo_ms")
    private Long tempoMinimoMs;

    @Column("tempo_maximo_ms")
    private Long tempoMaximoMs;
}
