package com.centroweg.iot.time_trial_api.core.repository;

import com.centroweg.iot.time_trial_api.core.domain.Volta;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;

public interface VoltaRepository extends CassandraRepository<Volta, String> {

    @Query("SELECT * FROM volta WHERE sessao_id = ?0")
    List<Volta> findBySessaoId(String sessaoId);
}
