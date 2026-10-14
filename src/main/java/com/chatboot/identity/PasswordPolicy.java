package com.chatboot.identity;

public class PasswordPolicy {
    public void check(String password) {
        if (password == null || password.length() < 8) {
            throw new PasswordTooShortException();
        }
    }

    public static class PasswordTooShortException extends RuntimeException {}
}
