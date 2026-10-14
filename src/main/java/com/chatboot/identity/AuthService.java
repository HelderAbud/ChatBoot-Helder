package com.chatboot.identity;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public RegisteredUser register(String email, String password, String workspaceName) {
        throw new UnsupportedOperationException("user persistence arrives with the database migration");
    }

    public IssuedToken login(String email, String password) {
        throw new UnsupportedOperationException("user persistence arrives with the database migration");
    }
}
