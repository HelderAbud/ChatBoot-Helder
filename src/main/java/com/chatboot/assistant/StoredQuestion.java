package com.chatboot.assistant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class StoredQuestion {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    private boolean abstained;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "question_id", nullable = false)
    private List<StoredCitation> citations = new ArrayList<>();

    protected StoredQuestion() {}

    public StoredQuestion(
            UUID id, UUID userId, UUID workspaceId, String question, String answer, boolean abstained) {
        this.id = id;
        this.userId = userId;
        this.workspaceId = workspaceId;
        this.question = question;
        this.answer = answer;
        this.abstained = abstained;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isAbstained() {
        return abstained;
    }

    public List<StoredCitation> getCitations() {
        return citations;
    }
}
