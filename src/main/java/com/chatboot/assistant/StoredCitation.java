package com.chatboot.assistant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "question_citations")
public class StoredCitation {
    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "chunk_id", nullable = false)
    private String chunkId;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String heading;

    protected StoredCitation() {}

    public StoredCitation(UUID id, UUID workspaceId, String chunkId, String documentName, String heading) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.chunkId = chunkId;
        this.documentName = documentName;
        this.heading = heading;
    }

    public String getChunkId() {
        return chunkId;
    }
}
