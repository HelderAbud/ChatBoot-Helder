package com.chatboot.assistant;

import java.util.List;

public class EvidenceGate {
    public enum Decision { ABSTAIN, ANSWER }

    public Decision decide(List<RetrievedChunk> chunks, double minimumScore) {
        if (chunks == null || chunks.isEmpty()) {
            return Decision.ABSTAIN;
        }
        double best = chunks.stream().mapToDouble(RetrievedChunk::score).max().orElse(0);
        if (best < minimumScore) {
            return Decision.ABSTAIN;
        }
        return Decision.ANSWER;
    }
}
