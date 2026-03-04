package com.centroweg.iot.time_trial_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getKeyspaceName() {
        return "time_trial";
    }

    @Override
    protected String getContactPoints() {
        return "localhost";
    }

    @Override
    protected int getPort() {
        return 9042;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.centroweg.iot.time_trial_api.core.domain;"};
    }
}
