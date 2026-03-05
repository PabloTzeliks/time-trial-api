package com.centroweg.iot.time_trial_api.repository;

import com.centroweg.iot.time_trial_api.domain.Race;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RaceRepository extends CassandraRepository<Race, UUID> {
}
