package dev.langchain4j.community.store.oracle.spring;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Oracle chat memory Spring Boot auto-configuration.
 */
@Testcontainers(disabledWithoutDocker = true)
class OracleMemoryStoreAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OracleMemoryStoreAutoConfiguration.class))
            .withUserConfiguration(OracleTestContainerDataSourceConfiguration.class);

    /**
     * Verifies that the configured table is created and chat messages can be persisted,
     * loaded, and deleted through the auto-configured {@link ChatMemoryStore}.
     */
    @Test
    void should_create_table_and_store_and_load_and_delete_messages() {
        String tableName = "CHAT_MEMORY_" + ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
        String memoryId = "mem-" + ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);

        contextRunner
                .withPropertyValues(
                        "langchain4j.community.oracle.chat-memory.enabled=true",
                        "langchain4j.community.oracle.chat-memory.table-name=" + tableName,
                        "langchain4j.community.oracle.chat-memory.create-table=true",
                        "langchain4j.community.oracle.chat-memory.content-column-type=CLOB"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ChatMemoryStore.class);

                    ChatMemoryStore chatMemoryStore = context.getBean(ChatMemoryStore.class);
                    assertTableExists(context.getBean(DataSource.class), tableName);

                    List<ChatMessage> messages = List.of(
                            SystemMessage.from("You are helpful."),
                            UserMessage.from("hello")
                    );

                    chatMemoryStore.updateMessages(memoryId, messages);
                    List<ChatMessage> loaded = chatMemoryStore.getMessages(memoryId);
                    assertThat(loaded).hasSize(2);

                    chatMemoryStore.deleteMessages(memoryId);
                    assertThat(chatMemoryStore.getMessages(memoryId)).isEmpty();
                });
    }

    private static void assertTableExists(DataSource dataSource, String tableName) {
        String sql = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?";
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName.toUpperCase());
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to verify table existence: " + tableName, e);
        }
    }
}
