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

    @Test
    void doesNotAttachTextBeforeTheFirstHeading() {
        String markdown = """
                antes do titulo
                # Acesso
                login
                """;

        List<MarkdownChunk> chunks = new MarkdownChunker().chunk(markdown);

        assertEquals(1, chunks.size());
        assertEquals("Acesso", chunks.get(0).heading());
        assertEquals("login", chunks.get(0).body());
    }

    @Test
    void recognizesTheFirstHeadingAfterABom() {
        List<MarkdownChunk> chunks = new MarkdownChunker().chunk("\uFEFF# Acesso\nlogin\n");

        assertEquals(1, chunks.size());
        assertEquals("Acesso", chunks.get(0).heading());
        assertEquals("login", chunks.get(0).body());
    }

    @Test
    void keepsAHashtagLineInsideTheSection() {
        String markdown = """
                # Acesso
                #hashtag
                """;

        List<MarkdownChunk> chunks = new MarkdownChunker().chunk(markdown);

        assertEquals(1, chunks.size());
        assertEquals("Acesso", chunks.get(0).heading());
        assertEquals("#hashtag", chunks.get(0).body());
    }
}
