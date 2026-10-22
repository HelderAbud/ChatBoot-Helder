package com.chatboot.identity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PasswordPolicy.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registersAndReturnsIds() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID workspaceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(authService.register("ana@farol.example", "senha-ok", "Farol"))
                .thenReturn(new RegisteredUser(userId, workspaceId));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@farol.example","password":"senha-ok","workspaceName":"Farol"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.workspaceId").value(workspaceId.toString()));
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        when(authService.register(any(), any(), any())).thenThrow(new DuplicateEmailException());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@farol.example","password":"senha-ok","workspaceName":"Farol"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shortPasswordReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@farol.example","password":"1234567","workspaceName":"Farol"}
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void longPasswordReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@farol.example","password":"%s","workspaceName":"Farol"}
                                """.formatted("a".repeat(73))))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void blankEmailReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"  ","password":"senha-ok","workspaceName":"Farol"}
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void nullByteInEmailReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana\\u0000@farol.example\",\"password\":\"senha-ok\",\"workspaceName\":\"Farol\"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void nullByteInWorkspaceNameReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@farol.example\",\"password\":\"senha-ok\",\"workspaceName\":\"Farol\\u0000\"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void emailLongerThanColumnReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"senha-ok","workspaceName":"Farol"}
                                """.formatted("a".repeat(321))))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void workspaceNameLongerThanColumnReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@farol.example","password":"senha-ok","workspaceName":"%s"}
                                """.formatted("a".repeat(201))))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void loginReturnsBearerToken() throws Exception {
        when(authService.login("ana@farol.example", "senha-ok")).thenReturn(new IssuedToken("token-1"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@farol.example","password":"senha-ok"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-1"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void invalidCredentialsReturn401() throws Exception {
        when(authService.login(any(), any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@farol.example","password":"errada"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nullByteInLoginEmailReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana\\u0000@farol.example\",\"password\":\"senha-ok\"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(), any());
    }
}
