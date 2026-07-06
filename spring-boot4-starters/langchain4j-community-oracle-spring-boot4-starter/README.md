# LangChain4j Community Oracle Spring Boot 4 Starter

Spring Boot starter that auto-configures LangChain4j Oracle-backed stores.

It creates:

- an `EmbeddingStore<TextSegment>` backed by `OracleEmbeddingStore`
- a `ChatMemoryStore` backed by `OracleChatMemoryStore`

Both stores are created only when a `DataSource` bean is available and the corresponding feature is enabled.

## Requirements

- Java 17+
- Spring Boot 4.x
- Oracle Database with vector support for embedding-store use cases
- A configured Spring `DataSource`

## Installation

Add the starter to your application:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-community-oracle-spring-boot4-starter</artifactId>
    <version>${langchain4j-community.version}</version>
</dependency>
```

This starter also brings in the Oracle JDBC and LangChain4j Oracle dependencies declared in `pom.xml`.

## Quick Start

Configure your Oracle `DataSource` and the LangChain4j Oracle stores:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//localhost:1521/FREEPDB1
    username: system
    password: oracle

langchain4j:
  community:
    oracle:
      embeddingstore:
        enabled: true
        table-name: embeddings
        create-table: CREATE_IF_NOT_EXISTS
        create-vector-index: CREATE_NONE
        exact-search: true
      chat-memory:
        enabled: true
        table-name: chat_memory
        create-table: true
        memory-id-column-name: memory_id
        content-column-name: content
        content-column-type: CLOB
```

Then inject the stores where you need them:

```java
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Service;

@Service
class AiStorageService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatMemoryStore chatMemoryStore;

    AiStorageService(EmbeddingStore<TextSegment> embeddingStore,
                     ChatMemoryStore chatMemoryStore) {
        this.embeddingStore = embeddingStore;
        this.chatMemoryStore = chatMemoryStore;
    }
}
```

## Auto-Configured Beans

### Embedding Store

`OracleEmbeddingStoreAutoConfiguration` creates an `EmbeddingStore<TextSegment>` bean when:

- `langchain4j.community.oracle.embeddingstore.enabled=true`, or the property is omitted
- an Oracle-compatible `DataSource` bean exists
- no other `EmbeddingStore` bean is already defined
- `langchain4j.community.oracle.embeddingstore.table-name` is set

### Chat Memory Store

`OracleMemoryStoreAutoConfiguration` creates a `ChatMemoryStore` bean when:

- `langchain4j.community.oracle.chat-memory.enabled=true`, or the property is omitted
- an Oracle-compatible `DataSource` bean exists
- no other `ChatMemoryStore` bean is already defined
- `langchain4j.community.oracle.chat-memory.table-name` is not blank; it defaults to `chat_memory`

## Configuration Properties

### Embedding Store

Prefix: `langchain4j.community.oracle.embeddingstore`

| Property | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Enables or disables Oracle embedding store auto-configuration. |
| `table-name` | none | Required. Oracle table used to store embeddings. |
| `create-table` | `CREATE_NONE` | Table creation strategy passed to `OracleEmbeddingStore`. |
| `exact-search` | `false` | Enables exact vector search instead of approximate search. |
| `create-vector-index` | `CREATE_NONE` | Vector index creation strategy passed to `OracleEmbeddingStore`. |

Common `CreateOption` values include `CREATE_NONE` and `CREATE_IF_NOT_EXISTS`.

### Chat Memory

Prefix: `langchain4j.community.oracle.chat-memory`

| Property | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Enables or disables Oracle chat memory auto-configuration. |
| `table-name` | `chat_memory` | Oracle table used to persist chat memory entries. |
| `create-table` | `true` | Creates the chat memory table when the store is built. |
| `memory-id-column-name` | `MEMORY_ID` | Oracle column used to store chat memory IDs. |
| `content-column-name` | `CONTENT` | Oracle column used to store serialized chat messages. |
| `content-column-type` | `CLOB` | Content storage type. Use `CLOB` for broad compatibility or `JSON` for native Oracle JSON columns. |

## Disabling Auto-Configuration

Disable either store independently:

```yaml
langchain4j:
  community:
    oracle:
      embeddingstore:
        enabled: false
      chat-memory:
        enabled: false
```

You can also provide your own `EmbeddingStore` or `ChatMemoryStore` bean. The starter backs off when a user-defined bean of the same type exists.

## Running Tests

From the repository root, run only this module's unit tests:

```bash
mvn -pl spring-boot4-starters/langchain4j-community-oracle-spring-boot4-starter \
  -Dmaven.javadoc.skip=true \
  test
```

Run full verification (unit + integration tests):

```bash
mvn -pl spring-boot4-starters/langchain4j-community-oracle-spring-boot4-starter \
  -Dmaven.javadoc.skip=true \
  verify
```

If you want an isolated Maven cache during test runs:

```bash
mvn -pl spring-boot4-starters/langchain4j-community-oracle-spring-boot4-starter \
  -Dmaven.repo.local=$(pwd)/.m2-local-oracle-tests \
  -Dmaven.javadoc.skip=true \
  verify
```

The integration tests start a `gvenzl/oracle-free:23-slim-faststart` container automatically, so you do not need to provide `ORACLE_JDBC_URL`, `ORACLE_JDBC_USER`, or `ORACLE_JDBC_PASSWORD` for `*IT` tests. A compatible container runtime (Docker or Podman configured for Testcontainers) must be running.

## Project Layout

```text
src/main/java/dev/langchain4j/community/store/oracle/spring/
  OracleEmbeddingStoreAutoConfiguration.java
  OracleEmbeddingStoreProperties.java
  OracleMemoryStoreAutoConfiguration.java
  OracleMemoryStoreProperties.java

src/main/resources/META-INF/spring/
  org.springframework.boot.autoconfigure.AutoConfiguration.imports
```
