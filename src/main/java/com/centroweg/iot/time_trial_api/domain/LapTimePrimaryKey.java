package com.centroweg.iot.time_trial_api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
public class LapTimePrimaryKey implements Serializable {

    @PrimaryKeyColumn(name = "race_id", type = PrimaryKeyType.PARTITIONED)
    private UUID raceId;

    @PrimaryKeyColumn(name = "car_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private UUID carId;

    @PrimaryKeyColumn(name = "lap_number", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private Integer lapNumber;
}
