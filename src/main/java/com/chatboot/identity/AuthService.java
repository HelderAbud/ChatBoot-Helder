package com.chatboot.identity;

import java.sql.SQLException;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String UNKNOWN_USER_HASH = new BCryptPasswordEncoder(10).encode("chatboot-unknown-user");

    private final UserAccountRepository users;
    private final WorkspaceRepository workspaces;
    private final PasswordEncoder encoder;
    private final JwtService tokens;

    public AuthService(
            UserAccountRepository users,
            WorkspaceRepository workspaces,
            PasswordEncoder encoder,
            JwtService tokens) {
        this.users = users;
        this.workspaces = workspaces;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @Transactional
    public RegisteredUser register(String email, String password, String workspaceName) {
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        try {
            users.save(new UserAccount(userId, email, encoder.encode(password)));
        } catch (DataIntegrityViolationException ex) {
            if (isUniqueViolation(ex)) {
                throw new DuplicateEmailException();
            }
            throw ex;
        }
        workspaces.save(new WorkspaceEntity(workspaceId, workspaceName, userId));
        return new RegisteredUser(userId, workspaceId);
    }

    public IssuedToken login(String email, String password) {
        if (password == null || email == null) {
            encoder.matches("chatboot-unknown-user", UNKNOWN_USER_HASH);
            throw new InvalidCredentialsException();
        }
        UserAccount user = users.findByEmail(email).orElse(null);
        if (user == null) {
            encoder.matches(password, UNKNOWN_USER_HASH);
            throw new InvalidCredentialsException();
        }
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        WorkspaceEntity workspace =
                workspaces.findByOwnerUserId(user.getId()).orElseThrow(InvalidCredentialsException::new);
        return new IssuedToken(tokens.issue(user.getId(), workspace.getId()));
    }

    private static boolean isUniqueViolation(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
