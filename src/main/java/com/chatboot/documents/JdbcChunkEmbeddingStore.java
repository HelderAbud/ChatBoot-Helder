package com.chatboot.documents;

import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcChunkEmbeddingStore implements ChunkEmbeddingStore {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcChunkEmbeddingStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(UUID chunkId, float[] embedding) {
        jdbc.update(
                "update document_chunks set embedding = cast(:embedding as vector) where id = :id",
                new MapSqlParameterSource()
                        .addValue("embedding", literal(embedding))
                        .addValue("id", chunkId));
    }

    public static String literal(float[] embedding) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(Float.toString(embedding[i]));
        }
        return builder.append(']').toString();
    }
}
