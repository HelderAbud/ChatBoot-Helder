package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class OllamaChatAdapterTest {

    @Test
    void readsTheAnswerAndTheCitedIds() {
        ChatDraft draft = OllamaChatAdapter.parse("{\"answer\":\"R$ 99\",\"citedChunkIds\":[\"chunk-1\"]}");

        assertEquals("R$ 99", draft.answer());
        assertEquals(List.of("chunk-1"), draft.citedChunkIds());
    }

    @Test
    void invalidJsonBecomesAnAnswerWithoutCitations() {
        ChatDraft draft = OllamaChatAdapter.parse("nao encontrei");

        assertEquals("", draft.answer());
        assertTrue(draft.citedChunkIds().isEmpty());
    }
}
