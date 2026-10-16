package com.chatboot.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JdbcChunkEmbeddingStoreTest {

    @Test
    void writesTheVectorAsABracketedList() {
        assertEquals("[1.5,2.0]", JdbcChunkEmbeddingStore.literal(new float[] {1.5f, 2.0f}));
    }
}
