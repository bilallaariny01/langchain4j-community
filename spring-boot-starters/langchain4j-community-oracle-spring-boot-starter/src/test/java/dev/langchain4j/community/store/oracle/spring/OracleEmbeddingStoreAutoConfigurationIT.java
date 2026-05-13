package dev.langchain4j.community.store.oracle.spring;

import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

/**
 * Integration tests for Oracle embedding store Spring Boot auto-configuration.
 */
@Testcontainers(disabledWithoutDocker = true)
class OracleEmbeddingStoreAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OracleEmbeddingStoreAutoConfiguration.class))
            .withUserConfiguration(OracleTestContainerDataSourceConfiguration.class);

    /**
     * Verifies that the embedding table is created and embeddings can be stored and searched successfully.
     */
    @Test
    void should_create_table_and_store_and_search_embedding() {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        TextSegment segment = TextSegment.from("Berlin is the capital of Germany");
        Embedding embedding = embeddingModel.embed(segment.text()).content();

        String tableName = randomTableName();

        contextRunner
                .withPropertyValues(properties(tableName))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(EmbeddingStore.class);
                    assertTableExists(context.getBean(DataSource.class), tableName);

                    @SuppressWarnings("unchecked")
                    EmbeddingStore<TextSegment> embeddingStore = (EmbeddingStore<TextSegment>) context.getBean(EmbeddingStore.class);

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    EmbeddingMatch<TextSegment> match = embeddingStore
                            .search(EmbeddingSearchRequest.builder()
                                    .queryEmbedding(embedding)
                                    .maxResults(1)
                                    .build())
                            .matches()
                            .get(0);

                    assertThat(match.score()).isCloseTo(1, withPercentage(1));
                    assertThat(match.embeddingId()).isEqualTo(id);
                    assertThat(match.embedding()).isEqualTo(embedding);
                    assertThat(match.embedded()).isEqualTo(segment);
                });
    }

    /**
     * Verifies that a user-provided {@link EmbeddingModel} bean is respected by auto-configuration.
     */
    @Test
    void should_respect_embedding_model_bean() {
        String tableName = randomTableName();
        contextRunner
                .withConfiguration(AutoConfigurations.of(TestEmbeddingModelAutoConfiguration.class))
                .withPropertyValues(properties(tableName))
                .run(context -> {
                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel)
                            .isNotNull()
                            .isExactlyInstanceOf(AllMiniLmL6V2EmbeddingModel.class);
                    assertThat(context).hasSingleBean(EmbeddingStore.class);
                    assertTableExists(context.getBean(DataSource.class), tableName);
                });
    }

    private static void assertTableExists(DataSource dataSource, String tableName) {
        String sql = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?";
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName.toUpperCase());
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                int tableCount = resultSet.getInt(1);
                assertThat(tableCount).isEqualTo(1);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to verify table existence: " + tableName, e);
        }
    }

    private static String randomTableName() {
        return "EMBEDDINGS_" + ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
    }

    private static String[] properties(String tableName) {
        return new String[] {
                "langchain4j.community.oracle.embeddingstore.enabled=true",
                "langchain4j.community.oracle.embeddingstore.table-name=" + tableName,
                "langchain4j.community.oracle.embeddingstore.create-table=CREATE_IF_NOT_EXISTS",
                "langchain4j.community.oracle.embeddingstore.create-vector-index=CREATE_NONE",
                "langchain4j.community.oracle.embeddingstore.exact-search=true"
        };
    }
}
