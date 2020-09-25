package edu.rit.codelanx.data.loader;

import com.codelanx.commons.data.types.MySQL;
import com.codelanx.commons.util.cache.Cache;
import edu.rit.codelanx.config.ConfigKey;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StateBuilder;
import edu.rit.codelanx.data.types.Library;

import java.util.List;

public class SQLStorageAdapter implements StorageAdapter {
    private final Cache<MySQL> db;

    public SQLStorageAdapter() {
        this.db = MySQL.newCache(ConfigKey.getSQLPreferences(), ConfigKey.SQL_KEEPALIVE_MS.as(long.class));
    }

    @Override
    public Library getLibrary() {
        return null;
    }

    @Override
    public <R extends State> List<R> getState(Class<R> type) {
        return null;
    }

    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        return null;
    }

    @Override
    public <R extends State> R insert(long id, StateBuilder<R> builder) {
        return null;
    }

    @Override
    public void loadAll() {

    }
}
