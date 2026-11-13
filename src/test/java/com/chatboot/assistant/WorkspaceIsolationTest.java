package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.chatboot.documents.JdbcChunkEmbeddingStore;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@Testcontainers
class WorkspaceIsolationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres"));

    @Test
    void keepsOneWorkspaceOutOfAnother() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource());

        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UUID workspaceA = UUID.randomUUID();
        UUID workspaceB = UUID.randomUUID();
        UUID documentA = UUID.randomUUID();
        UUID documentB = UUID.randomUUID();
        float[] embedding = new float[1024];
        embedding[0] = 1f;
        String vector = JdbcChunkEmbeddingStore.literal(embedding);

        insertUser(jdbc, userA, "a@farol.example");
        insertUser(jdbc, userB, "b@farol.example");
        insertWorkspace(jdbc, workspaceA, "A", userA);
        insertWorkspace(jdbc, workspaceB, "B", userB);
        insertDocument(jdbc, documentA, workspaceA, "a.md");
        insertDocument(jdbc, documentB, workspaceB, "b.md");
        insertChunk(jdbc, documentA, workspaceA, vector);
        insertChunk(jdbc, documentB, workspaceB, vector);

        JdbcChunkSearch search = new JdbcChunkSearch(jdbc);

        assertEquals(List.of("a.md"), names(search.search(workspaceA, embedding, 5)));
        assertEquals(List.of("b.md"), names(search.search(workspaceB, embedding, 5)));
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "select count(*) from documents where id = :id and workspace_id = :workspaceId",
                        new MapSqlParameterSource().addValue("id", documentA).addValue("workspaceId", workspaceB),
                        Long.class));
    }

    private static DataSource dataSource() {
        return new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static List<String> names(List<ScoredChunk> chunks) {
        return chunks.stream().map(ScoredChunk::documentName).toList();
    }

    private static void insertUser(NamedParameterJdbcTemplate jdbc, UUID id, String email) {
        jdbc.update(
                "insert into users (id, email, password_hash) values (:id, :email, :passwordHash)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("email", email)
                        .addValue("passwordHash", "hash"));
    }

    private static void insertWorkspace(NamedParameterJdbcTemplate jdbc, UUID id, String name, UUID ownerId) {
        jdbc.update(
                "insert into workspaces (id, name, owner_user_id) values (:id, :name, :ownerId)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("name", name)
                        .addValue("ownerId", ownerId));
    }

    private static void insertDocument(NamedParameterJdbcTemplate jdbc, UUID id, UUID workspaceId, String name) {
        jdbc.update(
                "insert into documents (id, workspace_id, original_name, status) values (:id, :workspaceId, :name, 'COMPLETED')",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("workspaceId", workspaceId)
                        .addValue("name", name));
    }

    private static void insertChunk(
            NamedParameterJdbcTemplate jdbc, UUID documentId, UUID workspaceId, String vector) {
        jdbc.update(
                """
                insert into document_chunks (id, document_id, workspace_id, chunk_index, heading, body, embedding)
                values (:id, :documentId, :workspaceId, 0, 'Titulo', 'corpo', cast(:embedding as vector))
                """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("documentId", documentId)
                        .addValue("workspaceId", workspaceId)
                        .addValue("embedding", vector));
    }
}
