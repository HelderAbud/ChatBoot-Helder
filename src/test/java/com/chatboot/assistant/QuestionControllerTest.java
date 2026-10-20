package com.chatboot.assistant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuestionControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID WORKSPACE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questions;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsTheWorkspaceAnswer() throws Exception {
        when(questions.ask(eq(USER_ID), eq(WORKSPACE_ID), eq("qual o preco"), eq(1.01d)))
                .thenReturn(new QuestionResponse(
                        "O plano Farol custa R$ 99.",
                        false,
                        List.of(new Citation("chunk-1", "catalogo-planos.md", "Plano Farol"))));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(tokenFor(WORKSPACE_ID)));

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"qual o preco\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("O plano Farol custa R$ 99."))
                .andExpect(jsonPath("$.abstained").value(false))
                .andExpect(jsonPath("$.citations[0].chunkId").value("chunk-1"))
                .andExpect(jsonPath("$.citations[0].documentName").value("catalogo-planos.md"))
                .andExpect(jsonPath("$.citations[0].heading").value("Plano Farol"));
    }

    @Test
    void returnsServiceUnavailableWhenTheModelIsDown() throws Exception {
        when(questions.ask(eq(USER_ID), eq(WORKSPACE_ID), eq("qual o preco"), eq(1.01d)))
                .thenThrow(new EmbeddingUnavailableException());
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(tokenFor(WORKSPACE_ID)));

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"qual o preco\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void rejectsABlankQuestionWithoutCallingTheModel() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(tokenFor(WORKSPACE_ID)));

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":null}"))
                .andExpect(status().isBadRequest());

        verify(questions, never()).ask(any(), any(), any(), anyDouble());
    }

    @Test
    void rejectsAWhitespaceQuestionWithoutCallingTheModel() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(tokenFor(WORKSPACE_ID)));

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verify(questions, never()).ask(any(), any(), any(), anyDouble());
    }

    private static Jwt tokenFor(UUID workspaceId) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(USER_ID.toString())
                .claim("workspaceId", workspaceId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(1800))
                .build();
    }
}
