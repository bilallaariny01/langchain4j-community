package dev.langchain4j.community.store.oracle.spring;

import java.sql.SQLException;

import javax.sql.DataSource;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.oracle.OracleChatMemoryStore;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice tests for Oracle chat memory auto-configuration behavior.
 */
class OracleMemoryStoreAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OracleMemoryStoreAutoConfiguration.class));

    /**
     * Verifies that a {@link ChatMemoryStore} bean is created when Oracle support is enabled
     * and a {@link DataSource} bean is available.
     */
    @Test
    void createsOracleMemoryStoreWhenEnabledAndDataSourceProvided() {
        if (!shouldRunWithRealOracle()) {
            return;
        }
        contextRunner
                .withUserConfiguration(DataSourceConfiguration.class)
                .withPropertyValues(
                        "langchain4j.community.oracle.chat-memory.enabled=true",
                        "langchain4j.community.oracle.chat-memory.table-name=chat_memory_test",
                        "langchain4j.community.oracle.chat-memory.create-table=true"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ChatMemoryStore.class);
                    assertThat(context).hasSingleBean(OracleMemoryStoreProperties.class);
                });
    }

    /**
     * Verifies that the new Oracle chat memory builder options are bound without needing
     * a live database connection when table creation is disabled.
     */
    @Test
    void createsOracleChatMemoryStoreWhenCreateTableDisabled() {
        contextRunner
                .withUserConfiguration(DataSourceConfiguration.class)
                .withPropertyValues(
                        "langchain4j.community.oracle.chat-memory.enabled=true",
                        "langchain4j.community.oracle.chat-memory.table-name=chat_memory_test",
                        "langchain4j.community.oracle.chat-memory.create-table=false",
                        "langchain4j.community.oracle.chat-memory.memory-id-column-name=session_id",
                        "langchain4j.community.oracle.chat-memory.content-column-name=messages_json",
                        "langchain4j.community.oracle.chat-memory.content-column-type=JSON"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ChatMemoryStore.class);
                    assertThat(context.getBean(ChatMemoryStore.class)).isInstanceOf(OracleChatMemoryStore.class);
                    assertThat(context).hasSingleBean(OracleMemoryStoreProperties.class);
                });
    }

    /**
     * Verifies that auto-configuration is disabled when the feature flag is set to false.
     */
    @Test
    void doesNotCreateOracleMemoryStoreWhenDisabled() {
        contextRunner
                .withUserConfiguration(DataSourceConfiguration.class)
                .withPropertyValues("langchain4j.community.oracle.chat-memory.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ChatMemoryStore.class));
    }

    /**
     * Verifies that no memory store is created when no Oracle {@link javax.sql.DataSource} bean exists.
     */
    @Test
    void doesNotCreateOracleMemoryStoreWhenNoDataSourceBeanExists() {
        contextRunner
                .withPropertyValues("langchain4j.community.oracle.chat-memory.enabled=true")
                .run(context -> assertThat(context).doesNotHaveBean(ChatMemoryStore.class));
    }

    /**
     * Verifies that startup fails fast when the chat memory table name property is blank.
     */
    @Test
    void failsWhenTableNameIsBlank() {
        contextRunner
                .withUserConfiguration(DataSourceConfiguration.class)
                .withPropertyValues(
                        "langchain4j.community.oracle.chat-memory.enabled=true",
                        "langchain4j.community.oracle.chat-memory.table-name="
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("langchain4j.community.oracle.chat-memory.table-name must be set");
                });
    }

    /**
     * Verifies that auto-configuration backs off when the user already defines a {@link ChatMemoryStore} bean.
     */
    @Test
    void backsOffWhenUserProvidesChatMemoryStore() {
        contextRunner
                .withUserConfiguration(DataSourceConfiguration.class, ExistingChatMemoryStoreConfiguration.class)
                .withPropertyValues("langchain4j.community.oracle.chat-memory.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatMemoryStore.class);
                    assertThat(context.getBean(ChatMemoryStore.class))
                            .isSameAs(ExistingChatMemoryStoreConfiguration.USER_DEFINED_STORE);
                });
    }

    /**
     * Test Oracle data source configuration with defaults suitable for local integration runs.
     */
    @Configuration(proxyBeanMethods = false)
    static class DataSourceConfiguration {

        @Bean
        DataSource dataSource() throws SQLException {
            OracleDataSource dataSource = new OracleDataSource();
            dataSource.setURL(System.getenv().getOrDefault("ORACLE_JDBC_URL", "jdbc:oracle:thin:@//localhost:1521/FREEPDB1"));
            dataSource.setUser(System.getenv().getOrDefault("ORACLE_JDBC_USER", "system"));
            dataSource.setPassword(System.getenv().getOrDefault("ORACLE_JDBC_PASSWORD", "oracle"));
            return dataSource;
        }
    }

    /**
     * Test configuration that provides a user-defined {@link ChatMemoryStore} bean.
     */
    @Configuration(proxyBeanMethods = false)
    static class ExistingChatMemoryStoreConfiguration {

        private static final ChatMemoryStore USER_DEFINED_STORE = new ChatMemoryStore() {
            @Override
            public java.util.List<dev.langchain4j.data.message.ChatMessage> getMessages(Object memoryId) {
                throw new UnsupportedOperationException("Test stub");
            }

            @Override
            public void updateMessages(Object memoryId, java.util.List<dev.langchain4j.data.message.ChatMessage> messages) {
                throw new UnsupportedOperationException("Test stub");
            }

            @Override
            public void deleteMessages(Object memoryId) {
                throw new UnsupportedOperationException("Test stub");
            }
        };

        @Bean
        ChatMemoryStore chatMemoryStore() {
            return USER_DEFINED_STORE;
        }
    }

    private static boolean shouldRunWithRealOracle() {
        return System.getenv("ORACLE_JDBC_URL") != null
                && !System.getenv("ORACLE_JDBC_URL").isBlank()
                && System.getenv("ORACLE_JDBC_USER") != null
                && !System.getenv("ORACLE_JDBC_USER").isBlank()
                && System.getenv("ORACLE_JDBC_PASSWORD") != null
                && !System.getenv("ORACLE_JDBC_PASSWORD").isBlank();
    }
}
