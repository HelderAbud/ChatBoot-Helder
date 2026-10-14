package com.chatboot.assistant;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CitationChecker {
    public List<String> keep(Set<String> retrievedIds, List<String> citedIds) {
        LinkedHashSet<String> kept = new LinkedHashSet<>();
        for (String cited : citedIds) {
            if (retrievedIds.contains(cited)) {
                kept.add(cited);
            }
        }
        return new ArrayList<>(kept);
    }
}
