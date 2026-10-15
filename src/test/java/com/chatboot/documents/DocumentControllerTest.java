package com.chatboot.documents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MarkdownUploadValidator.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void rejectsPdfWithoutCallingService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "manual.pdf", "application/pdf", "# Titulo".getBytes());

        mockMvc.perform(multipart("/api/v1/documents").file(file)).andExpect(status().isBadRequest());

        verify(documentService, never()).store(any(), any(), any());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hidesDocumentFromAnotherWorkspace() throws Exception {
        UUID workspaceA = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID workspaceB = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID documentId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        when(documentService.find(workspaceB, documentId)).thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(tokenFor(workspaceB)));

        mockMvc.perform(get("/api/v1/documents/" + documentId)).andExpect(status().isNotFound());

        verify(documentService).find(workspaceB, documentId);
        verify(documentService, never()).find(workspaceA, documentId);
    }

    @Test
    void returnsServiceUnavailableWhenStoreFails() throws Exception {
        UUID workspaceId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        when(documentService.store(eq(workspaceId), eq("catalogo-planos.md"), any()))
                .thenReturn(new StoredDocument(UUID.randomUUID(), "FAILED"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(tokenFor(workspaceId)));
        MockMultipartFile file =
                new MockMultipartFile("file", "catalogo-planos.md", "text/markdown", "# Plano Farol".getBytes());

        mockMvc.perform(multipart("/api/v1/documents").file(file)).andExpect(status().isServiceUnavailable());
    }

    @Test
    void returnsDocumentNameAndFailureMessage() throws Exception {
        UUID workspaceId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID documentId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        when(documentService.find(workspaceId, documentId))
                .thenReturn(Optional.of(new DocumentDetails(
                        documentId, "catalogo-planos.md", "FAILED", "embedding unavailable")));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(tokenFor(workspaceId)));

        mockMvc.perform(get("/api/v1/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalName").value("catalogo-planos.md"))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureMessage").value("embedding unavailable"));
    }

    private static Jwt tokenFor(UUID workspaceId) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("11111111-1111-1111-1111-111111111111")
                .claim("workspaceId", workspaceId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(1800))
                .build();
    }
}
