package com.chatboot.documents;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    static final String TEXT_STORED = "TEXT_STORED";

    private final LocalFileStorage storage;
    private final MarkdownChunker chunker;
    private final DocumentRepository documents;
    private final DocumentChunkRepository chunks;

    public DocumentService(
            LocalFileStorage storage,
            MarkdownChunker chunker,
            DocumentRepository documents,
            DocumentChunkRepository chunks) {
        this.storage = storage;
        this.chunker = chunker;
        this.documents = documents;
        this.chunks = chunks;
    }

    @Transactional
    public StoredDocument store(UUID workspaceId, String originalName, byte[] content) {
        try {
            UUID documentId = storage.store(content);
            documents.save(new DocumentEntity(documentId, workspaceId, originalName, TEXT_STORED));
            int index = 0;
            for (MarkdownChunk chunk : chunker.chunk(new String(content, StandardCharsets.UTF_8))) {
                chunks.save(new DocumentChunkEntity(
                        UUID.randomUUID(), documentId, workspaceId, index, chunk.heading(), chunk.body()));
                index++;
            }
            return new StoredDocument(documentId, TEXT_STORED);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to store markdown", ex);
        }
    }

    public Optional<StoredDocument> find(UUID workspaceId, UUID documentId) {
        return documents
                .findByIdAndWorkspaceId(documentId, workspaceId)
                .map(entity -> new StoredDocument(entity.getId(), entity.getStatus()));
    }
}
