package com.centroweg.iot.time_trial_api.repository;

import com.centroweg.iot.time_trial_api.domain.LapTime;
import com.centroweg.iot.time_trial_api.domain.LapTimePrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LapTimeRepository extends CassandraRepository<LapTime, LapTimePrimaryKey> {

    @Query("SELECT * FROM lap_times WHERE race_id = ?0")
    List<LapTime> findByRaceId(UUID raceId);

    @Query("SELECT * FROM lap_times WHERE race_id = ?0 AND car_id = ?1")
    List<LapTime> findByRaceIdAndCarId(UUID raceId, UUID carId);
}
