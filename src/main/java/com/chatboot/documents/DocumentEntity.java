package com.chatboot.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String status;

    protected DocumentEntity() {}

    public DocumentEntity(UUID id, UUID workspaceId, String originalName, String status) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.originalName = originalName;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
