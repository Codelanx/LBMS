package edu.rit.codelanx;

import com.codelanx.commons.config.ConfigFile;
import com.codelanx.commons.config.DataHolder;
import com.codelanx.commons.config.RelativePath;
import com.codelanx.commons.data.types.Json;
import com.codelanx.commons.data.types.MySQL;

@RelativePath("config.json")

/**
 * Represents a list of possible configuration values available to our system
 */
public enum ConfigKey implements ConfigFile {
    STORAGE_TYPE("storage-type", "json"), //sql, yml, or json
    MAX_BACKUP_FILES("max-backup-files", 100),
    SQL_KEEPALIVE_MS("sql.keep-alive-ms", 1000 * 60 * 10), //10 minutes
    SQL_USER("sql.auth.username", "swen-262"),
    SQL_PASS("sql.auth.password", "nopermsanyhow"),
    SQL_ADDR("sql.auth.iaddress", "home.rogue.ninja"),
    SQL_PORT("sql.auth.port", 3306),
    SQL_BASE("sql.auth.database", "lbms"),
    ;

    private static final DataHolder<Json> DATA = new DataHolder<>(Json.class);
    private final String path;
    private final Object defaultValue;

    private ConfigKey(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public Object getDefault() {
        return this.defaultValue;
    }

    @Override
    public DataHolder<Json> getData() {
        return DATA;
    }

    public static MySQL.ConnectionPrefs getSQLPreferences() {
        return new MySQL.ConnectionPrefs(SQL_USER, SQL_PASS, SQL_ADDR, SQL_BASE, SQL_PORT);
    }
}
