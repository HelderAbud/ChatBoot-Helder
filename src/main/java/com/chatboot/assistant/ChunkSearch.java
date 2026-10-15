package com.chatboot.assistant;

import java.util.List;
import java.util.UUID;

public interface ChunkSearch {
    List<ScoredChunk> search(UUID workspaceId, float[] embedding, int limit);
}
