package com.chatboot.documents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalFileStorage {
    private final Path root;

    @Autowired
    public LocalFileStorage(@Value("${chatboot.storage-dir}") String directory) {
        this(Path.of(directory));
    }

    public LocalFileStorage(Path root) {
        this.root = root;
    }

    public UUID store(byte[] content) throws IOException {
        Files.createDirectories(root);
        UUID id = UUID.randomUUID();
        Files.write(root.resolve(id + ".md"), content);
        return id;
    }
}
