package com.chatboot.identity;

import java.nio.charset.StandardCharsets;

public class PasswordPolicy {
    static final int MAX_BYTES = 72;

    public void check(String password) {
        if (password == null || password.length() < 8) {
            throw new PasswordTooShortException();
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new PasswordTooLongException();
        }
    }

    public static class PasswordTooShortException extends RuntimeException {}

    public static class PasswordTooLongException extends RuntimeException {}
}
