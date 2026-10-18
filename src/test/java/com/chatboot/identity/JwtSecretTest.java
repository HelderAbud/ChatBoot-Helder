package com.chatboot.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtSecretTest {

    @Test
    void rejectsASecretShorterThanThirtyTwoBytes() {
        assertThrows(IllegalStateException.class, () -> JwtSecret.require("a".repeat(31)));
    }

    @Test
    void acceptsThirtyTwoBytes() {
        assertEquals(32, JwtSecret.require("a".repeat(32)).getEncoded().length);
    }
}
