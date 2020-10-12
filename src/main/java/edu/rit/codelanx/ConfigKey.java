package edu.rit.codelanx;

import com.codelanx.commons.config.ConfigFile;
import com.codelanx.commons.config.DataHolder;
import com.codelanx.commons.config.RelativePath;
import com.codelanx.commons.data.types.Json;
import com.codelanx.commons.data.types.MySQL;
import com.codelanx.commons.util.cache.Cache;

/**
 * Represents a list of possible configuration values available to our system
 *
 * @author sja9291  Spencer Alderman
 */
@RelativePath("config.json")
public enum ConfigKey implements ConfigFile {
    STORAGE_TYPE("storage-type", "json"), //sql, yml, or json
    MAX_BACKUP_FILES("max-backup-files", 100), //maximum backups of bad data files
    SQL_KEEPALIVE_MS("sql.keep-alive-ms", 1000 * 60 * 10), //10 minutes
    //the remaining details are for retrieving sql preferences
    SQL_USER("sql.auth.username", "swen-262"),
    SQL_PASS("sql.auth.password", "nopermsanyhow"),
    SQL_ADDR("sql.auth.iaddress", "home.rogue.ninja"),
    SQL_PORT("sql.auth.port", 3306),
    SQL_BASE("sql.auth.database", "lbms"),
    ;

    //holds a reference to our file data
    private static final DataHolder<Json> DATA = new DataHolder<>(Json.class);
    private final String path; //path in the file
    private final Object defaultValue;

    private ConfigKey(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object getDefault() {
        return this.defaultValue;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataHolder<Json> getData() {
        return DATA;
    }

    /**
     * Evalulates the current settings to produce a valid
     * {@link MySQL.ConnectionPrefs}
     *
     * @return The preferences as noted in the current configuration
     */
    public static MySQL.ConnectionPrefs getSQLPreferences() {
        return new MySQL.ConnectionPrefs(SQL_USER, SQL_PASS, SQL_ADDR, SQL_BASE, SQL_PORT);
    }

    /**
     * Builds a new {@link Cache Cache<MySQL>} from settings specified in the
     * configuration file for this enum
     *
     * @return A pre-configured and ready-to-use {@link MySQL} cache
     * @see Cache
     * @see MySQL
     */
    public static Cache<MySQL> newDBCache() {
        return MySQL.newCache(ConfigKey.getSQLPreferences(), SQL_KEEPALIVE_MS.as(Long.class));
    }
}
