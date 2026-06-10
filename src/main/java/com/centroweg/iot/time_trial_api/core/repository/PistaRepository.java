package com.centroweg.iot.time_trial_api.core.repository;

import com.centroweg.iot.time_trial_api.core.domain.Pista;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface PistaRepository extends CassandraRepository<Pista, String> { }
