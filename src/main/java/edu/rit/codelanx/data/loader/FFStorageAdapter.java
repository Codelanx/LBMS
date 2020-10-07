package edu.rit.codelanx.data.loader;

import com.codelanx.commons.data.FileDataType;
import com.codelanx.commons.data.types.Json;
import com.codelanx.commons.data.types.XML;
import edu.rit.codelanx.ConfigKey;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.storage.RelativeStorage;
import edu.rit.codelanx.data.storage.StateStorage;
import edu.rit.codelanx.data.storage.field.DataField;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FFStorageAdapter implements StorageAdapter {

    private static final String DATA_FILE_NAME = "data";
    private static final Pattern FF_DATA_SEARCH = Pattern.compile(DATA_FILE_NAME + "\\d*\\.(json|yml)");
    private final Class<? extends FileDataType> type;
    private final RelativeStorage dataBank;
    private final DataStorage storage;
    private volatile Library library;

    public FFStorageAdapter(DataStorage storage, String type) {
        this.type = FileDataType.fromString(type);
        this.storage = storage;
        if (this.type == null) {
            throw new IllegalStateException("Cannot interpret config's storage-type: " + type);
        } else if (this.type == XML.class) {
            throw new UnsupportedOperationException("Sorry! We don't support XML"); //Why? because the parser is broken
        }
        this.dataBank = new RelativeStorage(storage);
    }

    @Override
    public Library getLibrary() {
        return this.library;
    }

    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        return builder.buildObj(this.storage, builder.getType().getNextID());
    }

    @Override
    public void loadAll() throws IOException {
        if (this.library != null) {
            throw new IllegalStateException("File contents already loaded!");
        }
        String ext = (this.type == Json.class ? ".json" : ".yml");
        File ref = new File(DATA_FILE_NAME + ext); //only supporting json/yml flatfiles
        if (!ref.exists()) {
            this.library = Library.create()
                    .setValue(Library.Field.MONEY, BigDecimal.ZERO)
                    .build(this.storage);
            return;
        }
        FileDataType data = FileDataType.newInstance(this.type, ref);
        List<?> read = data.getMutable("data").as(List.class);
        if (ref.exists() && read == null) {
            this.errorRecovery(ref, ext);
            return;
        }
        Object bad = read.stream()
                .filter(o -> !(o instanceof State))
                .findAny().orElse(null);
        if (bad != null) {
            System.err.println("Bad value while parsing input file: " + bad);
            this.errorRecovery(ref, ext);
            return;
        }
        read.stream()
                .map(o -> (State) o)
                .forEach(this.dataBank::addState);
        Library lib = this.storage.query(Library.class).results().findAny().orElse(null);
        if (lib == null) {
            //no library defined yet, make one!
            lib = Library.create()
                    .setValue(Library.Field.MONEY, BigDecimal.ZERO)
                    .build(this.storage);
        }
        this.library = lib;
    }

    @Override
    public void saveAll() throws IOException {
        //TODO: Write all data to files
    }

    private void errorRecovery(File ref, String ext) throws IOException {
        System.err.println("Bad data file provided, backing up and starting fresh");
        if (!ref.isAbsolute()) {
            ref = ref.getAbsoluteFile();
        }
        File[] avail = ref.getParentFile().listFiles(File::isFile);
        if (avail == null) {
            //is this a bad description? it's pretty much what I'll say in response to this error ever happening
            throw new IllegalStateException("wat");
        }
        long next = Arrays.stream(avail).map(File::getName)
                .filter(s -> {
                    Matcher m = FF_DATA_SEARCH.matcher(s);
                    return m.matches() && m.group(1).equalsIgnoreCase(ext);
                })
                .count();
        if (next > ConfigKey.MAX_BACKUP_FILES.as(int.class)) {
            throw new IllegalStateException("Ran out of room for backups, aborting!");
        }
        Files.move(ref.toPath(), new File(DATA_FILE_NAME + next + ext).toPath());
    }

    @Override
    public <R extends State> Stream<R> handleQuery(StateQuery<R> query) {
        Class<R> type = query.getType();
        StateStorage<R> data = this.dataBank.getStateStorage(type);
        return query.locateLocal(data);
    }

    @Override
    public <R extends State> R loadState(long id, Class<R> type) {
        //flatfile storage is preloaded, no on-demand loading necessary
        return this.dataBank.getStateStorage(type).getByID(id);
    }

    @Override
    public <R extends State, E> Stream<R> loadState(Class<R> type, DataField<E> field, E value) {
        StateStorage<R> data = this.dataBank.getStateStorage(type);
        return data.streamLoaded()
                .filter(s -> Objects.equals(field.get(s), value));
    }

    @Override
    public <E> void notifyUpdate(State state, DataField<E> field, E value) {
        //well, nothing to be done then
    }

    @Override
    public void remove(State state) {
        //nothing to do here, data lives in the caches
    }

    @Override
    public boolean isCached() {
        return true;
    }
}
