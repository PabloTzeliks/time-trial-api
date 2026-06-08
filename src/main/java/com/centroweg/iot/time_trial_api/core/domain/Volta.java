package com.centroweg.iot.time_trial_api.core.domain;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("volta")
public class Volta {

    @PrimaryKeyColumn(name = "sessao_id", type = PrimaryKeyType.PARTITIONED)
    private String sessaoId;

    @PrimaryKeyColumn(name = "carro_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private String carroId;

    @PrimaryKeyColumn(name = "ts", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Long ts;

    @Column("duracao_ms")
    private Long duracaoMs;
}
