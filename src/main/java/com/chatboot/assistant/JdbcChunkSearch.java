package com.chatboot.assistant;

import com.chatboot.documents.JdbcChunkEmbeddingStore;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcChunkSearch implements ChunkSearch {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcChunkSearch(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ScoredChunk> search(UUID workspaceId, float[] embedding, int limit) {
        return jdbc.query(
                """
                select c.id, d.original_name, c.heading, c.body,
                       1 - (c.embedding <=> cast(:embedding as vector)) as score
                from document_chunks c
                join documents d on d.id = c.document_id
                where c.workspace_id = :workspaceId
                  and c.embedding is not null
                order by c.embedding <=> cast(:embedding as vector)
                limit :limit
                """,
                new MapSqlParameterSource()
                        .addValue("embedding", JdbcChunkEmbeddingStore.literal(embedding))
                        .addValue("workspaceId", workspaceId)
                        .addValue("limit", limit),
                (rs, row) -> new ScoredChunk(
                        rs.getObject("id").toString(),
                        rs.getString("original_name"),
                        rs.getString("heading"),
                        rs.getString("body"),
                        rs.getDouble("score")));
    }
}
