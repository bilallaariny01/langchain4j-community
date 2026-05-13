package dev.langchain4j.community.store.oracle.spring;



import javax.sql.DataSource;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.oracle.CreateOption;
import dev.langchain4j.store.embedding.oracle.OracleEmbeddingStore;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.community.store.oracle.spring.OracleEmbeddingStoreProperties.CONFIG_PREFIX;

/**
 * Spring Boot auto-configuration for {@link OracleEmbeddingStore}.
 * <p>
 * Creates an {@link EmbeddingStore} bean when Oracle embedding store support is enabled and a
 * {@link DataSource} is available in the application context.
 */
@AutoConfiguration
@EnableConfigurationProperties(OracleEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({ DataSource.class, OracleEmbeddingStore.class })
@ConditionalOnBean(DataSource.class)
public class OracleEmbeddingStoreAutoConfiguration {

    /**
     * Builds an {@link OracleEmbeddingStore} from configured properties.
     *
     * @param dataSource configured JDBC data source
     * @param properties embedding store settings bound from Spring configuration
     * @return configured embedding store bean
     */
    @Bean
    @ConditionalOnMissingBean
    public EmbeddingStore<TextSegment> oracleEmbeddingStore(
            DataSource dataSource,
            OracleEmbeddingStoreProperties properties) {

        if (properties.getTableName() == null || properties.getTableName().isBlank()) {
            throw new IllegalStateException("Property " + CONFIG_PREFIX + ".table-name must be set");
        }

        CreateOption tableCreate = properties.getCreateTable();
        CreateOption vectorIndexCreate = properties.getCreateVectorIndex();

        // Build OracleEmbeddingStore
        return OracleEmbeddingStore.builder()
                .dataSource(dataSource)
                .embeddingTable(properties.getTableName(), tableCreate)
                .exactSearch(properties.isExactSearch())
                // Optional: create vector index if configured
                .vectorIndex(vectorIndexCreate)
                .build();
    }
}
