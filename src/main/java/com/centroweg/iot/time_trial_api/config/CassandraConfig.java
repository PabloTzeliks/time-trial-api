package com.centroweg.iot.time_trial_api.config;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.DriverConfigLoaderBuilderConfigurer;

@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name:time_trial}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points:localhost}")
    private String contactPoints;

    @Value("${spring.cassandra.port:9042}")
    private int port;

    @Value("${spring.cassandra.local-datacenter:dc1}")
    private String localDatacenter;

    @Value("${spring.cassandra.request.consistency:QUORUM}")
    private String consistencyLevel;

    /**
     * Quando true, habilita autenticação SigV4 (IAM) e TLS para o Amazon Keyspaces.
     * Em desenvolvimento local com Docker, mantenha como false (padrão).
     * Na AWS, injete AWS_KEYSPACES_ENABLED=true via Task Definition / variável de ambiente.
     */
    @Value("${aws.keyspaces.enabled:false}")
    private boolean keyspacesEnabled;

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.centroweg.iot.time_trial_api.core.domain"};
    }

    @Override
    protected DriverConfigLoaderBuilderConfigurer getDriverConfigLoaderBuilderConfigurer() {
        return builder -> {
            builder.withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistencyLevel);

            if (keyspacesEnabled) {
                // Amazon Keyspaces: autenticação via AWS SigV4 (IAM) + TLS obrigatório
                builder
                    .withString(DefaultDriverOption.AUTH_PROVIDER_CLASS,
                        "software.aws.mcs.auth.SigV4AuthProvider")
                    .withString(DefaultDriverOption.SSL_ENGINE_FACTORY_CLASS,
                        "DefaultSslEngineFactory")
                    .withBoolean(DefaultDriverOption.SSL_HOSTNAME_VALIDATION, false);
            }
        };
    }
}