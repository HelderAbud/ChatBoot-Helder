package com.chatboot.documents;

import java.util.UUID;

public interface ChunkEmbeddingStore {
    void save(UUID chunkId, float[] embedding);
}
