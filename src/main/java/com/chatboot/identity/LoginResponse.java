package com.chatboot.identity;

public record LoginResponse(String accessToken, long expiresIn, String tokenType) {}
