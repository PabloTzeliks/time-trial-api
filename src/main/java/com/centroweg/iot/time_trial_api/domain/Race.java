package com.centroweg.iot.time_trial_api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("races")
public class Race {

    @PrimaryKey("race_id")
    private UUID raceId;

    @Column("name")
    private String name;

    @Column("total_laps")
    private Integer totalLaps;

    @Column("status")
    private String status;

    @Column("start_time")
    private Instant startTime;

    @Column("end_time")
    private Instant endTime;

    @Column("created_at")
    private Instant createdAt;
}
