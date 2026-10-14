package com.chatboot.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "document_chunks")
public class DocumentChunkEntity {
    @Id
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(nullable = false)
    private String heading;

    @Column(nullable = false)
    private String body;

    protected DocumentChunkEntity() {}

    public DocumentChunkEntity(
            UUID id, UUID documentId, UUID workspaceId, int chunkIndex, String heading, String body) {
        this.id = id;
        this.documentId = documentId;
        this.workspaceId = workspaceId;
        this.chunkIndex = chunkIndex;
        this.heading = heading;
        this.body = body;
    }
}
