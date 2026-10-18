package com.chatboot.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chatboot.assistant.EmbeddingPort;
import com.chatboot.assistant.EmbeddingUnavailableException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    private static final UUID WORKSPACE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Mock
    private LocalFileStorage storage;

    @Mock
    private MarkdownChunker chunker;

    @Mock
    private DocumentRepository documents;

    @Mock
    private DocumentChunkRepository chunks;

    @Mock
    private EmbeddingPort embeddings;

    @Mock
    private ChunkEmbeddingStore vectors;

    private DocumentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentService(storage, chunker, documents, chunks, embeddings, vectors);
    }

    @Test
    void marksDocumentFailedWhenEmbeddingIsUnavailable() throws Exception {
        when(storage.store(any())).thenReturn(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        when(chunker.chunk(any())).thenReturn(List.of(new MarkdownChunk("Plano Farol", "R$ 99")));
        when(embeddings.embed("Plano Farol\nR$ 99")).thenThrow(new EmbeddingUnavailableException());

        StoredDocument stored = service.store(
                WORKSPACE_ID, "catalogo-planos.md", "# Plano Farol\nR$ 99".getBytes(StandardCharsets.UTF_8));

        assertEquals("FAILED", stored.status());
        verify(vectors, never()).save(any(), any());
    }

    @Test
    void marksDocumentFailedWhenSavingTheVectorBreaks() throws Exception {
        float[] vector = new float[1024];
        when(storage.store(any())).thenReturn(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        when(chunker.chunk(any())).thenReturn(List.of(new MarkdownChunk("Plano Farol", "R$ 99")));
        when(embeddings.embed("Plano Farol\nR$ 99")).thenReturn(vector);
        doThrow(new RuntimeException("db")).when(vectors).save(any(UUID.class), any(float[].class));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> service.store(
                        WORKSPACE_ID, "catalogo-planos.md", "# Plano Farol\nR$ 99".getBytes(StandardCharsets.UTF_8)));

        assertEquals("failed to store markdown", error.getMessage());
        ArgumentCaptor<DocumentEntity> saved = ArgumentCaptor.forClass(DocumentEntity.class);
        verify(documents, org.mockito.Mockito.atLeastOnce()).save(saved.capture());
        assertEquals("FAILED", saved.getValue().getStatus());
        assertEquals("failed to store markdown", saved.getValue().getFailureMessage());
    }

    @Test
    void marksDocumentCompletedWhenEmbeddingIsStored() throws Exception {
        float[] vector = new float[1024];
        when(storage.store(any())).thenReturn(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        when(chunker.chunk(any())).thenReturn(List.of(new MarkdownChunk("Plano Farol", "R$ 99")));
        when(embeddings.embed("Plano Farol\nR$ 99")).thenReturn(vector);

        StoredDocument stored = service.store(
                WORKSPACE_ID, "catalogo-planos.md", "# Plano Farol\nR$ 99".getBytes(StandardCharsets.UTF_8));

        assertEquals("COMPLETED", stored.status());
        verify(vectors).save(any(UUID.class), org.mockito.ArgumentMatchers.eq(vector));
    }
}
