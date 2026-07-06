package dev.langchain4j.community.store.oracle.spring;

import dev.langchain4j.store.memory.chat.oracle.OracleChatMemoryStore.ContentColumnType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Oracle chat memory auto-configuration.
 * <p>
 * Prefix: {@value #CONFIG_PREFIX}
 */
@ConfigurationProperties(prefix = OracleMemoryStoreProperties.CONFIG_PREFIX)
public class OracleMemoryStoreProperties {

    public static final String CONFIG_PREFIX = "langchain4j.community.oracle.chat-memory";

    /**
     * Enables/disables Oracle chat memory auto-configuration.
     */
    private boolean enabled = true;

    /**
     * Oracle table used to persist chat memory entries.
     */
    private String tableName = "chat_memory";

    /**
     * Whether to create the chat memory table when the store is built.
     */
    private boolean createTable = true;

    /**
     * Oracle column used to store chat memory IDs.
     */
    private String memoryIdColumnName = "MEMORY_ID";

    /**
     * Oracle column used to store serialized chat messages.
     */
    private String contentColumnName = "CONTENT";

    /**
     * Oracle column type used to store serialized chat messages.
     */
    private ContentColumnType contentColumnType = ContentColumnType.CLOB;

    /**
     * @return whether chat memory auto-configuration is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled whether chat memory auto-configuration should be enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return chat memory table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName chat memory table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return whether the chat memory table should be created
     */
    public boolean isCreateTable() {
        return createTable;
    }

    /**
     * @param createTable whether the chat memory table should be created
     */
    public void setCreateTable(boolean createTable) {
        this.createTable = createTable;
    }

    /**
     * @return chat memory ID column name
     */
    public String getMemoryIdColumnName() {
        return memoryIdColumnName;
    }

    /**
     * @param memoryIdColumnName chat memory ID column name
     */
    public void setMemoryIdColumnName(String memoryIdColumnName) {
        this.memoryIdColumnName = memoryIdColumnName;
    }

    /**
     * @return chat memory content column name
     */
    public String getContentColumnName() {
        return contentColumnName;
    }

    /**
     * @param contentColumnName chat memory content column name
     */
    public void setContentColumnName(String contentColumnName) {
        this.contentColumnName = contentColumnName;
    }

    /**
     * @return chat memory content column type
     */
    public ContentColumnType getContentColumnType() {
        return contentColumnType;
    }

    /**
     * @param contentColumnType chat memory content column type
     */
    public void setContentColumnType(ContentColumnType contentColumnType) {
        this.contentColumnType = contentColumnType;
    }
}
