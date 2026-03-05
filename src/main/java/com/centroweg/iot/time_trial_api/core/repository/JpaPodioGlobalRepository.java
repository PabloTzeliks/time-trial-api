package com.centroweg.iot.time_trial_api.core.repository;

import com.centroweg.iot.time_trial_api.core.domain.PodioGlobal;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;

public interface JpaPodioGlobalRepository extends CassandraRepository<PodioGlobal, String> {

    @Query("SELECT * FROM podio_global WHERE agrupador = 'GERAL' LIMIT 10")
    List<PodioGlobal> buscarTop10();
}
