package dev.langchain4j.community.store.oracle.spring;

import java.time.Duration;

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
     * Time-to-live for a chat memory entry.
     * <p>
     * Spring Boot supports both ISO-8601 and simple suffix formats, for example:
     * {@code PT30S}, {@code PT5M}, {@code PT1H}, {@code P1D}, {@code 30s}, {@code 5m}, {@code 1h}, {@code 1d}.
     * <p>
     * For chat continuity and better user experience, a minimum of 7 days is recommended
     * (for example {@code P7D} or {@code 7d}).
     * <p>
     * Use {@code PT0S} to disable expiration.
     */
    private Duration ttl = Duration.ZERO;

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
     * @return configured chat memory TTL
     */
    public Duration getTtl() {
        return ttl;
    }

    /**
     * @param ttl chat memory TTL
     */
    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
