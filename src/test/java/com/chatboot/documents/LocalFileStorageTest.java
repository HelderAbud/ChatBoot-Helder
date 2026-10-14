package com.chatboot.documents;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalFileStorageTest {

    @Test
    void storesBytesUnderUuidFileName() throws Exception {
        Path directory = Files.createTempDirectory("chatboot-storage");
        byte[] content = "# Titulo".getBytes(StandardCharsets.UTF_8);

        UUID id = new LocalFileStorage(directory).store(content);

        Path file = directory.resolve(id + ".md");
        assertTrue(Files.exists(file));
        assertTrue(file.getFileName().toString().matches(
                "[0-9a-fA-F-]{36}\\.md"));
        assertArrayEquals(content, Files.readAllBytes(file));
    }
}
