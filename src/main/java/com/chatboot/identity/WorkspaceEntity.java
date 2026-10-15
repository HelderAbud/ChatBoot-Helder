package com.chatboot.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
public class WorkspaceEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    protected WorkspaceEntity() {}

    public WorkspaceEntity(UUID id, String name, UUID ownerUserId) {
        this.id = id;
        this.name = name;
        this.ownerUserId = ownerUserId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }
}
