package ru.ntzw.com.dt.server.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class StorableService implements Initializable, Disposable {

    private final Path storagePath;

    protected StorableService(Path storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public void init() throws Exception {
        ensureFileExists(storagePath);
        try(BufferedReader reader = Files.newBufferedReader(storagePath)) {
            load(reader);
        }
    }

    @Override
    public void dispose() throws Exception {
        ensureFileExists(storagePath);
        try(BufferedWriter writer = Files.newBufferedWriter(storagePath)) {
            store(writer);
        }
    }

    private void ensureFileExists(Path path) throws IOException {
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Files.createFile(path);
        } catch(FileAlreadyExistsException e) {
            //ignored
        }
    }

    protected abstract void load(BufferedReader reader) throws IOException;

    protected abstract void store(BufferedWriter writer) throws IOException;
}
