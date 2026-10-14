package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class EvidenceGateTest {

    private final EvidenceGate gate = new EvidenceGate();

    @Test
    void abstainsWhenNothingWasRetrieved() {
        assertEquals(EvidenceGate.Decision.ABSTAIN, gate.decide(List.of(), 0.50));
    }

    @Test
    void abstainsWhenBestScoreIsBelowTheMinimum() {
        List<RetrievedChunk> chunks = List.of(new RetrievedChunk("a", "catalogo-planos.md", "Plano Farol", 0.49));
        assertEquals(EvidenceGate.Decision.ABSTAIN, gate.decide(chunks, 0.50));
    }

    @Test
    void answersWhenBestScoreEqualsTheMinimum() {
        List<RetrievedChunk> chunks = List.of(new RetrievedChunk("a", "catalogo-planos.md", "Plano Farol", 0.50));
        assertEquals(EvidenceGate.Decision.ANSWER, gate.decide(chunks, 0.50));
    }
}
