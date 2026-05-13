package dev.langchain4j.community.store.oracle.spring;

import dev.langchain4j.store.embedding.oracle.CreateOption;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Oracle embedding store auto-configuration.
 * <p>
 * Prefix: {@value #CONFIG_PREFIX}
 */
@ConfigurationProperties(prefix = OracleEmbeddingStoreProperties.CONFIG_PREFIX)
public class OracleEmbeddingStoreProperties {

    public static final String CONFIG_PREFIX = "langchain4j.community.oracle.embeddingstore";

    /**
     * Enable/disable auto-config.
     */
    private boolean enabled = true;

    /**
     * Name of the embedding table.
     */
    private String tableName;

    /**
     * Whether to create the table if missing (depends on your CreateOption enum).
     */
    private CreateOption createTable = CreateOption.CREATE_NONE;

    /**
     * Whether search should be exact or approximate.
     */
    private boolean exactSearch = false;

    /**
     * Whether to create a vector index (IVF) (optional).
     */
    private CreateOption createVectorIndex = CreateOption.CREATE_NONE;

    /**
     * @return whether Oracle embedding store auto-configuration is enabled
     */
    public boolean isEnabled() { return enabled; }
    /**
     * @param enabled whether Oracle embedding store auto-configuration should be enabled
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * @return Oracle embedding table name
     */
    public String getTableName() { return tableName; }
    /**
     * @param tableName Oracle embedding table name
     */
    public void setTableName(String tableName) { this.tableName = tableName; }

    /**
     * @return table creation strategy
     */
    public CreateOption getCreateTable() { return createTable; }
    /**
     * @param createTable table creation strategy
     */
    public void setCreateTable(CreateOption createTable) { this.createTable = createTable; }

    /**
     * @return whether exact vector search should be used
     */
    public boolean isExactSearch() { return exactSearch; }
    /**
     * @param exactSearch whether exact vector search should be used
     */
    public void setExactSearch(boolean exactSearch) { this.exactSearch = exactSearch; }

    /**
     * @return vector index creation strategy
     */
    public CreateOption getCreateVectorIndex() { return createVectorIndex; }
    /**
     * @param createVectorIndex vector index creation strategy
     */
    public void setCreateVectorIndex(CreateOption createVectorIndex) { this.createVectorIndex = createVectorIndex; }
}
