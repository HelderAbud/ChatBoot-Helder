package com.chatboot.assistant;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OllamaChatAdapter implements ChatPort {
    private final RestClient client;
    private final String model;
    private final ObjectMapper mapper;

    public OllamaChatAdapter(
            @Value("${chatboot.ollama.base-url}") String baseUrl,
            @Value("${chatboot.ollama.chat-model}") String model,
            ObjectMapper mapper) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.model = model;
        this.mapper = mapper;
    }

    @Override
    public ChatDraft answer(String question, List<ScoredChunk> chunks) {
        Map<String, Object> body = Map.of(
                "model", model,
                "think", false,
                "stream", false,
                "messages", List.of(Map.of("role", "user", "content", prompt(question, chunks))));
        try {
            String raw = client.post().uri("/api/chat").body(body).retrieve().body(String.class);
            if (raw == null) {
                throw new EmbeddingUnavailableException();
            }
            String content = mapper.readTree(raw).path("message").path("content").asText("");
            return parse(content);
        } catch (EmbeddingUnavailableException ex) {
            throw ex;
        } catch (RestClientException | JacksonException ex) {
            throw new EmbeddingUnavailableException(ex);
        }
    }

    static ChatDraft parse(String content) {
        if (content == null) {
            return new ChatDraft("", List.of());
        }
        String json = content.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return new ChatDraft("", List.of());
        }
        try {
            JsonNode node = new ObjectMapper().readTree(json.substring(start, end + 1));
            if (!node.isObject() || !node.hasNonNull("answer")) {
                return new ChatDraft("", List.of());
            }
            List<String> ids = new ArrayList<>();
            JsonNode cited = node.get("citedChunkIds");
            if (cited != null && cited.isArray()) {
                for (JsonNode id : cited) {
                    if (id.isTextual()) {
                        ids.add(id.asText());
                    }
                }
            }
            return new ChatDraft(node.get("answer").asText(""), ids);
        } catch (JacksonException ex) {
            return new ChatDraft("", List.of());
        }
    }

    private static String prompt(String question, List<ScoredChunk> chunks) {
        StringBuilder builder = new StringBuilder();
        builder.append("Responda somente com um JSON {\"answer\":\"...\",\"citedChunkIds\":[\"...\"]}.\n");
        builder.append("Pergunta: ").append(question).append('\n');
        for (ScoredChunk chunk : chunks) {
            builder.append("id: ").append(chunk.id()).append('\n');
            builder.append("titulo: ").append(chunk.heading()).append('\n');
            builder.append("corpo: ").append(chunk.body()).append('\n');
        }
        return builder.toString();
    }
}
