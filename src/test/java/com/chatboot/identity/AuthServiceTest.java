package com.chatboot.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository users;

    @Mock
    private WorkspaceRepository workspaces;

    @Mock
    private JwtService tokens;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(10);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(users, workspaces, encoder, tokens);
    }

    @Test
    void storesHashedPasswordAndWorkspace() {
        when(users.existsByEmail("ana@farol.example")).thenReturn(false);

        RegisteredUser created = authService.register("ana@farol.example", "senha-ok", "Farol");

        ArgumentCaptor<UserAccount> user = ArgumentCaptor.forClass(UserAccount.class);
        ArgumentCaptor<WorkspaceEntity> workspace = ArgumentCaptor.forClass(WorkspaceEntity.class);
        verify(users).save(user.capture());
        verify(workspaces).save(workspace.capture());
        assertNotEquals("senha-ok", user.getValue().getPasswordHash());
        assertTrue(encoder.matches("senha-ok", user.getValue().getPasswordHash()));
        assertEquals(user.getValue().getId(), created.userId());
        assertEquals(workspace.getValue().getId(), created.workspaceId());
        assertEquals(created.userId(), workspace.getValue().getOwnerUserId());
    }

    @Test
    void rejectsDuplicateEmailWithoutSavingWorkspace() {
        when(users.existsByEmail("ana@farol.example")).thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> authService.register("ana@farol.example", "senha-ok", "Farol"));

        verify(workspaces, never()).save(any());
    }

    @Test
    void wrongPasswordDoesNotIssueToken() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserAccount account = new UserAccount(userId, "ana@farol.example", new BCryptPasswordEncoder(10).encode("senha-ok"));
        when(users.findByEmail("ana@farol.example")).thenReturn(Optional.of(account));

        assertThrows(InvalidCredentialsException.class, () -> authService.login("ana@farol.example", "errada"));

        verify(tokens, never()).issue(any(), any());
    }

    @Test
    void unknownEmailDoesNotIssueToken() {
        when(users.findByEmail("ana@farol.example")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login("ana@farol.example", "senha-ok"));

        verify(tokens, never()).issue(any(), any());
    }

    @Test
    void nullPasswordIsInvalidCredentials() {
        assertThrows(InvalidCredentialsException.class, () -> authService.login("ana@farol.example", null));

        verify(users, never()).findByEmail(any());
        verify(tokens, never()).issue(any(), any());
    }

    @Test
    void nullEmailIsInvalidCredentials() {
        assertThrows(InvalidCredentialsException.class, () -> authService.login(null, "senha-ok"));

        verify(users, never()).findByEmail(any());
        verify(tokens, never()).issue(any(), any());
    }

    @Test
    void unknownEmailStillChecksAPasswordHash() {
        PasswordEncoder passwords = org.mockito.Mockito.mock(PasswordEncoder.class);
        AuthService service = new AuthService(users, workspaces, passwords, tokens);
        when(users.findByEmail("ana@farol.example")).thenReturn(Optional.empty());
        when(passwords.matches(eq("senha-ok"), any())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.login("ana@farol.example", "senha-ok"));

        verify(passwords).matches(eq("senha-ok"), any());
        verify(tokens, never()).issue(any(), any());
    }

    @Test
    void duplicateInsertBecomesDuplicateEmail() {
        when(users.existsByEmail("ana@farol.example")).thenReturn(false);
        when(users.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(
                DuplicateEmailException.class,
                () -> authService.register("ana@farol.example", "senha-ok", "Farol"));

        verify(workspaces, never()).save(any());
    }

    @Test
    void loginIssuesTheWorkspaceIdThatWasStored() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID workspaceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(users.findByEmail("ana@farol.example"))
                .thenReturn(Optional.of(new UserAccount(userId, "ana@farol.example", encoder.encode("senha-ok"))));
        when(workspaces.findByOwnerUserId(userId))
                .thenReturn(Optional.of(new WorkspaceEntity(workspaceId, "Farol", userId)));
        when(tokens.issue(userId, workspaceId)).thenReturn("token-1");

        IssuedToken token = authService.login("ana@farol.example", "senha-ok");

        assertEquals("token-1", token.accessToken());
    }
}
