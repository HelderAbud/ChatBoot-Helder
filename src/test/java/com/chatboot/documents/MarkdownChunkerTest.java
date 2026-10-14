package com.chatboot.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MarkdownChunkerTest {

    @Test
    void splitsOnHeadingsAndKeepsCodeFenceIntact() {
        String markdown = """
                # Manual
                intro
                ## Acesso
                login
                ```
                # isto nao e secao
                ```
                fim
                """;
        List<MarkdownChunk> chunks = new MarkdownChunker().chunk(markdown);
        assertEquals(2, chunks.size());
        assertEquals("Manual", chunks.get(0).heading());
        assertEquals("Acesso", chunks.get(1).heading());
        assertTrue(chunks.get(1).body().contains("# isto nao e secao"));
    }
}
