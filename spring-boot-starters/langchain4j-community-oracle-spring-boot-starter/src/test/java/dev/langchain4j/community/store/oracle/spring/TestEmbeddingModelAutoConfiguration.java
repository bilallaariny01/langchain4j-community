package dev.langchain4j.community.store.oracle.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration exposing a deterministic {@link EmbeddingModel} bean for integration tests.
 */
@Configuration(proxyBeanMethods = false)
public class TestEmbeddingModelAutoConfiguration {

    /**
     * Creates the embedding model used by test contexts.
     *
     * @return embedding model bean for tests
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
