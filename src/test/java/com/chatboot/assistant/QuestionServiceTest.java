package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    private static final String ABSTAIN =
            "Não encontrei informação confiável na documentação disponível deste workspace.";
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID WORKSPACE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private EmbeddingPort embeddings;

    @Mock
    private ChunkSearch search;

    @Mock
    private ChatPort chat;

    @Mock
    private QuestionRepository questions;

    private QuestionService service;

    @BeforeEach
    void setUp() {
        service = new QuestionService(embeddings, search, chat, new EvidenceGate(), new CitationChecker(), questions);
    }

    @Test
    void abstainsWhenSearchIsEmpty() {
        when(embeddings.embed("qual o preco")).thenReturn(new float[] {0.1f});
        when(search.search(eq(WORKSPACE_ID), any(), eq(5))).thenReturn(List.of());

        QuestionResponse response = service.ask(USER_ID, WORKSPACE_ID, "qual o preco", 0.50);

        assertEquals(ABSTAIN, response.answer());
        assertTrue(response.abstained());
        assertTrue(response.citations().isEmpty());
        verify(chat, never()).answer(any(), any());
        StoredQuestion saved = savedQuestion();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(WORKSPACE_ID, saved.getWorkspaceId());
        assertEquals(ABSTAIN, saved.getAnswer());
        assertTrue(saved.isAbstained());
        assertTrue(saved.getCitations().isEmpty());
    }

    @Test
    void keepsOnlyCitationsFromTheRetrievedSet() {
        ScoredChunk chunk = new ScoredChunk("chunk-1", "catalogo-planos.md", "Plano Farol", "R$ 99", 0.50);
        when(embeddings.embed("qual o preco")).thenReturn(new float[] {0.2f});
        when(search.search(eq(WORKSPACE_ID), any(), eq(5))).thenReturn(List.of(chunk));
        when(chat.answer(eq("qual o preco"), any()))
                .thenReturn(new ChatDraft("O plano Farol custa R$ 99.", List.of("chunk-1", "fora-da-busca")));

        QuestionResponse response = service.ask(USER_ID, WORKSPACE_ID, "qual o preco", 0.50);

        assertEquals(false, response.abstained());
        assertEquals(1, response.citations().size());
        assertEquals("chunk-1", response.citations().get(0).chunkId());
        assertEquals("catalogo-planos.md", response.citations().get(0).documentName());
        assertEquals("Plano Farol", response.citations().get(0).heading());
        StoredQuestion saved = savedQuestion();
        assertEquals(1, saved.getCitations().size());
        assertEquals("chunk-1", saved.getCitations().get(0).getChunkId());
    }

    @Test
    void abstainsWhenBestScoreIsBelowTheMinimum() {
        ScoredChunk chunk = new ScoredChunk("chunk-1", "catalogo-planos.md", "Plano Farol", "R$ 99", 0.49);
        when(embeddings.embed("qual o preco")).thenReturn(new float[] {0.3f});
        when(search.search(eq(WORKSPACE_ID), any(), eq(5))).thenReturn(List.of(chunk));

        QuestionResponse response = service.ask(USER_ID, WORKSPACE_ID, "qual o preco", 0.50);

        assertEquals(ABSTAIN, response.answer());
        assertTrue(response.abstained());
        verify(chat, never()).answer(any(), any());
    }

    @Test
    void doesNotStoreAnAnswerWhenChatIsUnavailable() {
        ScoredChunk chunk = new ScoredChunk("chunk-1", "catalogo-planos.md", "Plano Farol", "R$ 99", 0.50);
        when(embeddings.embed("qual o preco")).thenReturn(new float[] {0.4f});
        when(search.search(eq(WORKSPACE_ID), any(), eq(5))).thenReturn(List.of(chunk));
        when(chat.answer(any(), any())).thenThrow(new EmbeddingUnavailableException());

        assertThrows(
                EmbeddingUnavailableException.class,
                () -> service.ask(USER_ID, WORKSPACE_ID, "qual o preco", 0.50));

        verify(questions, never()).save(any());
    }

    private StoredQuestion savedQuestion() {
        ArgumentCaptor<StoredQuestion> captor = ArgumentCaptor.forClass(StoredQuestion.class);
        verify(questions).save(captor.capture());
        return captor.getValue();
    }
}
