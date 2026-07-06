package dev.langchain4j.community.store.oracle.spring;

import javax.sql.DataSource;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.oracle.OracleChatMemoryStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.community.store.oracle.spring.OracleMemoryStoreProperties.CONFIG_PREFIX;

/**
 * Spring Boot auto-configuration for Oracle-backed {@link ChatMemoryStore}.
 * <p>
 * Creates a {@link ChatMemoryStore} bean backed by {@link OracleChatMemoryStore} when enabled and
 * when a {@link DataSource} bean is available.
 */
@AutoConfiguration
@EnableConfigurationProperties(OracleMemoryStoreProperties.class)
@ConditionalOnProperty(prefix = CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({DataSource.class, OracleChatMemoryStore.class, ChatMemoryStore.class})
@ConditionalOnBean(DataSource.class)
public class OracleMemoryStoreAutoConfiguration {

    /**
     * Builds an Oracle chat memory store from configured properties.
     *
     * @param dataSource configured Oracle data source
     * @param properties chat memory settings bound from Spring configuration
     * @return configured chat memory store bean
     * @throws Exception if store creation fails
     */
    @Bean
    @ConditionalOnMissingBean(ChatMemoryStore.class)
    public ChatMemoryStore oracleChatMemoryStore(
            DataSource dataSource,
            OracleMemoryStoreProperties properties) {

        if (properties.getTableName() == null || properties.getTableName().isBlank()) {
            throw new IllegalStateException("Property " + CONFIG_PREFIX + ".table-name must be set");
        }

        OracleChatMemoryStore.Builder builder = OracleChatMemoryStore.builder()
                .dataSource(dataSource)
                .tableName(properties.getTableName())
                .memoryIdColumnName(properties.getMemoryIdColumnName())
                .contentColumnName(properties.getContentColumnName())
                .contentColumnType(properties.getContentColumnType());

        if (properties.isCreateTable()) {
            builder.createTable();
        }

        return builder.build();
    }
}
