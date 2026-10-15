package com.chatboot.assistant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {
    static final String ABSTAIN =
            "Não encontrei informação confiável na documentação disponível deste workspace.";
    static final int SEARCH_LIMIT = 5;

    private final EmbeddingPort embeddings;
    private final ChunkSearch search;
    private final ChatPort chat;
    private final EvidenceGate gate;
    private final CitationChecker citations;
    private final QuestionRepository questions;

    public QuestionService(
            EmbeddingPort embeddings,
            ChunkSearch search,
            ChatPort chat,
            EvidenceGate gate,
            CitationChecker citations,
            QuestionRepository questions) {
        this.embeddings = embeddings;
        this.search = search;
        this.chat = chat;
        this.gate = gate;
        this.citations = citations;
        this.questions = questions;
    }

    @Transactional
    public QuestionResponse ask(UUID userId, UUID workspaceId, String question, double minimumScore) {
        List<ScoredChunk> found = search.search(workspaceId, embeddings.embed(question), SEARCH_LIMIT);
        List<RetrievedChunk> retrieved = found.stream()
                .map(chunk -> new RetrievedChunk(chunk.id(), chunk.documentName(), chunk.heading(), chunk.score()))
                .toList();
        if (gate.decide(retrieved, minimumScore) == EvidenceGate.Decision.ABSTAIN) {
            return store(userId, workspaceId, question, new QuestionResponse(ABSTAIN, true, List.of()));
        }
        ChatDraft draft = chat.answer(question, retrieved);
        Set<String> retrievedIds = retrieved.stream().map(RetrievedChunk::id).collect(Collectors.toSet());
        Map<String, ScoredChunk> byId = new LinkedHashMap<>();
        for (ScoredChunk chunk : found) {
            byId.putIfAbsent(chunk.id(), chunk);
        }
        List<Citation> published = new ArrayList<>();
        for (String id : citations.keep(retrievedIds, draft.citedChunkIds())) {
            ScoredChunk chunk = byId.get(id);
            if (chunk != null) {
                published.add(new Citation(chunk.id(), chunk.documentName(), chunk.heading()));
            }
        }
        return store(userId, workspaceId, question, new QuestionResponse(draft.answer(), false, published));
    }

    private QuestionResponse store(UUID userId, UUID workspaceId, String question, QuestionResponse response) {
        StoredQuestion stored = new StoredQuestion(
                UUID.randomUUID(), userId, workspaceId, question, response.answer(), response.abstained());
        for (Citation citation : response.citations()) {
            stored.getCitations()
                    .add(new StoredCitation(
                            UUID.randomUUID(),
                            workspaceId,
                            citation.chunkId(),
                            citation.documentName(),
                            citation.heading()));
        }
        questions.save(stored);
        return response;
    }
}
