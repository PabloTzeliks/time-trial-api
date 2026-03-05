package com.centroweg.iot.time_trial_api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lap_times")
public class LapTime {

    @PrimaryKey
    private LapTimePrimaryKey key;

    @Column("time_millis")
    private Long timeMillis;

    @Column("recorded_at")
    private Instant recordedAt;
}
