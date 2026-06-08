package com.centroweg.iot.time_trial_api.core.repository;

import com.centroweg.iot.time_trial_api.core.domain.Volta;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface VoltaRepository extends CassandraRepository<Volta, String> { }
