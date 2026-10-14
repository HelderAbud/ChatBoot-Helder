package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CitationCheckerTest {

    @Test
    void keepsOnlyRetrievedIdsInCitedOrderWithoutDuplicates() {
        List<String> kept = new CitationChecker().keep(Set.of("a", "b"), List.of("b", "c", "b", "a"));
        assertEquals(List.of("b", "a"), kept);
    }
}
