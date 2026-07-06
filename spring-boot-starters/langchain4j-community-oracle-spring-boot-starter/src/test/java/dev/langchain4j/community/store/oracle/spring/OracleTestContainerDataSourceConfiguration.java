package dev.langchain4j.community.store.oracle.spring;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Oracle Testcontainers data source for integration tests.
 */
@Configuration(proxyBeanMethods = false)
class OracleTestContainerDataSourceConfiguration {

    private static final OracleContainer ORACLE = new OracleContainer(
            DockerImageName.parse("gvenzl/oracle-free:23-faststart"));

    @Bean
    DataSource dataSource() throws Exception {
        ORACLE.start();

        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(ORACLE.getJdbcUrl());
        dataSource.setUser(ORACLE.getUsername());
        dataSource.setPassword(ORACLE.getPassword());
        return dataSource;
    }
}
