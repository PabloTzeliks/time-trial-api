package com.centroweg.iot.time_trial_api.core.repository;

import com.centroweg.iot.time_trial_api.core.domain.FeedRecente;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;

public interface JpaFeedRecenteRepository extends CassandraRepository<FeedRecente, String> {


}
