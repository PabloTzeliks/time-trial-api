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
@Table("cars")
public class Car {

    @PrimaryKey("car_id")
    private UUID carId;

    @Column("name")
    private String name;

    @Column("driver")
    private String driver;

    @Column("created_at")
    private Instant createdAt;
}
