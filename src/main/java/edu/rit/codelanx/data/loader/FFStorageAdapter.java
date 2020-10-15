package edu.rit.codelanx.data.loader;

import com.codelanx.commons.data.FileDataType;
import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.types.Json;
import com.codelanx.commons.data.types.XML;
import edu.rit.codelanx.ConfigKey;
import edu.rit.codelanx.data.DataSource;
import edu.rit.codelanx.data.state.StorageContainer;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.data.cache.StateStorage;
import edu.rit.codelanx.data.cache.field.DataField;
import edu.rit.codelanx.data.state.types.StateType;
import edu.rit.codelanx.data.state.types.Visitor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FFStorageAdapter implements StorageAdapter {

    private static final boolean DOES_BACKUP = true; //would backup normally, not _quite_ perfect on this yet
    private static final File BACKUP_FOLDER = new File("backup");
    private static final File DATA_FOLDER = new File("data");
    private final Class<? extends FileDataType> type;
    private final DataSource storage;
    private final Set<Class<? extends State>> loadedFromFile = new HashSet<>();
    private volatile Library library;
    private final AtomicBoolean modified = new AtomicBoolean(false);
    //this simply keeps our hard references, so they won't be unloaded at runtime
    private final Map<Class<? extends State>, Set<State>> states = new HashMap<>();

    protected FFStorageAdapter(DataSource storage) {
        this.storage = storage;
        this.type = null;
    }

    public FFStorageAdapter(DataSource storage, String type) {
        this.type = FileDataType.fromString(type);
        this.storage = storage;
        if (this.type == null) {
            throw new IllegalStateException("Cannot interpret config's storage-type: " + type);
        } else if (this.type == XML.class) {
            throw new UnsupportedOperationException("Sorry! We don't support XML"); //Why? because the parser is broken
        }
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataSource getAdaptee() {
        return this.storage;
    }
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Library getLibrary() {
        return this.library;
    }

    private static DataField<?>[] VISITOR_HACK = {Visitor.Field.FIRST, Visitor.Field.LAST, Visitor.Field.ADDRESS, Visitor.Field.PHONE};

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <R extends State> R insert(StateBuilder<R> builder) {
        //TODO: This is a hack, needs proper support from Composite keys (R2)
        if (builder.getType() == StateType.VISITOR) {
            Query<Visitor> query = this.storage.query(Visitor.class);
            for (DataField<?> field : VISITOR_HACK) {
                Object o = builder.getValue(field);
                query = query.isEqual((DataField<Object>) field, o);
            }
            if (query.results().count() > 0) {
                throw new IllegalArgumentException("Duplicate Visitor provided");
            }
        }
        this.modified.set(true);
        return builder.buildObj(this.storage, builder.getType().getNextID());
    }

    @Override
    public void loadAll() throws IOException {
        if (this.library != null) {
            throw new IllegalStateException("File contents already loaded");
        }
        if (!DATA_FOLDER.exists()) {
            //nothing to load
            this.library = Library.create()
                    .setValue(Library.Field.MONEY, BigDecimal.ZERO)
                    .build(this.storage);
            return;
        }
        String ext = (this.type == Json.class ? ".json" : ".yml");
        //TODO: Rewrite to use individual data files
        for (State.Type type : StateType.values()) {
            StorageContainer container = type.getConcreteType().getAnnotation(StorageContainer.class);
            if (container == null) {
                throw new IllegalStateException(type.getConcreteType() + " is missing @StorageContainer annotation");
            }
            File ref = new File(DATA_FOLDER, container.value() + ext);
            if (!ref.exists()) {
                if (type == StateType.LIBRARY) {
                    //create initial library
                    this.library = Library.create()
                            .setValue(Library.Field.MONEY, BigDecimal.ZERO)
                            .build(this.storage);
                }
                continue; //nothing to load
            }
            FileDataType data = FileDataType.newInstance(this.type, ref, (clazz, map) -> {
                if (clazz.isAssignableFrom(State.class)) {
                    throw new IllegalStateException("Cannot interpret type: " + clazz);
                }
                State.Type st = StateType.fromClass((Class<? extends State>) clazz);
                return st.getFileConstructor().create(this.storage, map);
            });
            List<?> read = data.getMutable("data").as(List.class);
            if (read == null || read.stream().anyMatch(o -> !(o instanceof State))) {
                //TODO: actual stderr here?
                System.err.println("Bad value while parsing input file: " + container.value());
                this.errorRecovery(ref, ext);
                return;
            }
            //they won't auto-map (anymore), because of the constructor mismatch
            read.stream()
                    .map(o -> (State) o)
                    .forEach(this.getStateCacheFor(type.getConcreteType())::add);
            this.loadedFromFile.add(type.getConcreteType());
            if (type == StateType.LIBRARY) {
                this.library = this.getAdaptee().query(Library.class).local()
                        .results().findAny().orElseGet(() -> Library.newEmptyLibrary(this.storage));
            }
        }
        this.modified.set(false); //initialized, at this point no modifications
    }

    @Override
    public void saveAll() throws IOException {
        if (!this.modified.get()) {
            return; //If never modified, do not save
        }
        if (this.library == null) {
            throw new IllegalStateException("Adapter was never initialized");
        }
        DATA_FOLDER.mkdir();
        String ext = (this.type == Json.class ? ".json" : ".yml");
        for (State.Type type : StateType.values()) {
            StorageContainer container = type.getConcreteType().getAnnotation(StorageContainer.class);
            if (container == null) {
                throw new IllegalStateException(type.getConcreteType() + " is missing @StorageContainer annotation");
            }
            File ref = new File(DATA_FOLDER, container.value() + ext);
            if (ref.exists() && !this.loadedFromFile.contains(type.getConcreteType())) {
                //File exists but was not what we loaded
                this.errorRecovery(ref, ext);
            }
            //fresh file refence
            FileDataType data = FileDataType.newInstance(this.type, "{}");
            data.set("data", this.storage.ofLoaded(type.getConcreteType()).collect(Collectors.toList()));
            data.save(ref);
        }
    }

    private void errorRecovery(File ref, String ext) throws IOException {
        if (!DOES_BACKUP) {
            return;
        }
        System.err.println("Bad data file provided, backing up and starting fresh");
        if (!ref.isAbsolute()) {
            ref = ref.getAbsoluteFile();
        }
        String nameExt = ref.getName();
        String name = nameExt.substring(0, nameExt.length() - ext.length());
        BACKUP_FOLDER.mkdir();
        File[] avail = BACKUP_FOLDER.listFiles(File::isFile);
        if (avail == null) {
            //is this a bad description? it's pretty much what I'll say
            // in response to this error ever happening
            throw new IllegalStateException("wat");
        }
        long next = Arrays.stream(avail)
                .map(File::getName)
                .filter(s -> s.startsWith(name))
                .map(s -> s.substring(0, name.length()))
                .count();
        if (next > ConfigKey.MAX_BACKUP_FILES.as(int.class)) {
            throw new IllegalStateException("Ran out of room for backups, aborting!");
        }
        Files.move(ref.toPath(), new File(BACKUP_FOLDER, name + next + ext).toPath());
    }

    @Override
    public <R extends State> Stream<R> handleQuery(StateQuery<R> query) {
        //Handle the query for information
        Class<R> type = query.getType();
        Set<R> back = this.getStateCacheFor(query.getType());
        if (back.isEmpty()) {
            return Stream.empty();
        }
        R example = back.iterator().next();
        Long specID = query.idSpecificLookup.get();
        if (specID > 0) {
            return (Stream<R>) example.getIDField().findStatesByValue(specID);
        }
        return query.locateLocal(this.getStateCacheFor(query.getType()).stream());
    }

    @Override
    public <R extends State> R loadState(long id, Class<R> type) {
        //flatfile storage is preloaded, no on-demand loading necessary
       /// return this.getAdaptee().getRelativeStorage().getStateStorage(type).getByID(id);
        return null;
    }

    @Override
    public <R extends State, E> Stream<R> loadState(Class<R> type, DataField<E> field, E value) {
        return this.getStateCacheFor(type).stream()
                .filter(s -> Objects.equals(field.get(s), value));
    }

    @Override
    public <E> void notifyUpdate(State state, DataField<E> field, E value) {
        this.modified.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(State state) {
        //TODO: Actually remove the state
        this.modified.set(true);
    }

    /**
     * {@inheritDoc}
     * @return {@code true} since this loads data into memory all the time
     */
    @Override
    public boolean isCached() {
        return true;
    }

    //gets our simple cache for hard references
    private <R extends State> Set<R> getStateCacheFor(Class<R> clazz) {
        return (Set<R>) this.states.computeIfAbsent(clazz, k -> new HashSet<>());
    }
}
