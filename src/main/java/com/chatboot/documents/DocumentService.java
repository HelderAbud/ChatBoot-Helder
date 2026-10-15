package com.chatboot.documents;

import com.chatboot.assistant.EmbeddingPort;
import com.chatboot.assistant.EmbeddingUnavailableException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    static final String TEXT_STORED = "TEXT_STORED";
    static final String COMPLETED = "COMPLETED";
    static final String FAILED = "FAILED";

    private final LocalFileStorage storage;
    private final MarkdownChunker chunker;
    private final DocumentRepository documents;
    private final DocumentChunkRepository chunks;
    private final EmbeddingPort embeddings;
    private final ChunkEmbeddingStore vectors;

    public DocumentService(
            LocalFileStorage storage,
            MarkdownChunker chunker,
            DocumentRepository documents,
            DocumentChunkRepository chunks,
            EmbeddingPort embeddings,
            ChunkEmbeddingStore vectors) {
        this.storage = storage;
        this.chunker = chunker;
        this.documents = documents;
        this.chunks = chunks;
        this.embeddings = embeddings;
        this.vectors = vectors;
    }

    @Transactional
    public StoredDocument store(UUID workspaceId, String originalName, byte[] content) {
        try {
            UUID documentId = storage.store(content);
            DocumentEntity document = new DocumentEntity(documentId, workspaceId, originalName, TEXT_STORED);
            documents.save(document);
            int index = 0;
            for (MarkdownChunk chunk : chunker.chunk(new String(content, StandardCharsets.UTF_8))) {
                UUID chunkId = UUID.randomUUID();
                chunks.save(new DocumentChunkEntity(
                        chunkId, documentId, workspaceId, index, chunk.heading(), chunk.body()));
                index++;
                try {
                    float[] embedding = embeddings.embed(chunk.heading() + "\n" + chunk.body());
                    vectors.save(chunkId, embedding);
                } catch (EmbeddingUnavailableException ex) {
                    document.markFailed("embedding unavailable");
                    documents.save(document);
                    return new StoredDocument(documentId, FAILED);
                }
            }
            document.markCompleted();
            documents.save(document);
            return new StoredDocument(documentId, COMPLETED);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to store markdown", ex);
        }
    }

    public Optional<DocumentDetails> find(UUID workspaceId, UUID documentId) {
        return documents
                .findByIdAndWorkspaceId(documentId, workspaceId)
                .map(entity -> new DocumentDetails(
                        entity.getId(), entity.getOriginalName(), entity.getStatus(), entity.getFailureMessage()));
    }
}
