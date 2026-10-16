package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JdbcChunkSearchTest {

    @Test
    void searchesOnlyCompletedDocumentsInTheWorkspace() {
        String sql = JdbcChunkSearch.SQL.toLowerCase();

        assertTrue(sql.contains("c.workspace_id = :workspaceid"));
        assertTrue(sql.contains("d.status = 'completed'"));
        assertTrue(sql.contains("c.embedding is not null"));
    }
}
