package com.chatboot.identity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();

    @Test
    void rejectsShortPassword() {
        assertThrows(PasswordPolicy.PasswordTooShortException.class, () -> policy.check("curta"));
    }

    @Test
    void acceptsEightCharacters() {
        assertDoesNotThrow(() -> policy.check("senha-ok"));
    }

    @Test
    void rejectsPasswordLongerThanBcryptCanStore() {
        assertThrows(PasswordPolicy.PasswordTooLongException.class, () -> policy.check("a".repeat(73)));
    }

    @Test
    void acceptsSeventyTwoBytes() {
        assertDoesNotThrow(() -> policy.check("a".repeat(72)));
    }
}
