package edu.rit.codelanx.data.loader;

import com.codelanx.commons.data.FileDataType;
import com.codelanx.commons.data.types.Json;
import com.codelanx.commons.data.types.XML;
import edu.rit.codelanx.config.ConfigKey;
import edu.rit.codelanx.data.state.StateBuilder;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.types.Library;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FFStorageAdapter implements StorageAdapter {

    private static final Pattern FF_DATA_SEARCH = Pattern.compile("config\\d*\\.(json|yml)");
    private final Class<? extends FileDataType> type;

    public FFStorageAdapter(String type) {
        this.type = FileDataType.fromString(type);
        if (this.type == null) {
            throw new IllegalStateException("Cannot interpret config's storage-type: " + type);
        } else if (this.type == XML.class) {
            throw new UnsupportedOperationException("Sorry! We don't support XML"); //Why? because the parser is broken
        }
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
    public void loadAll() throws IOException {
        String ext = (this.type == Json.class ? ".json" : ".yml");
        File ref = new File("config" + ext); //only supporting json/yml flatfiles
        FileDataType data = FileDataType.newInstance(this.type, ref);
        Object read = data.getMutable("data").as(List.class);
        if (ref.exists() && read == null) {
            System.err.println("Bad data file provided, backing up and starting fresh");
            File[] avail = ref.getParentFile().listFiles(File::isFile);
            if (avail == null) {
                //is this a bad description? it's pretty much what I'll say in response to this error ever happening
                throw new IllegalStateException("wat");
            }
            long next = Arrays.stream(avail).map(File::getName)
                    .filter(s -> FF_DATA_SEARCH.matcher(s).group(1).equalsIgnoreCase(ext))
                    .count();
            if (next > ConfigKey.MAX_BACKUP_FILES.as(int.class)) {
                throw new IllegalStateException("Ran out of room for backups, aborting!");
            }
            Files.move(ref.toPath(), new File("config" + next + ext).toPath());
            return;
        }
    }
}
