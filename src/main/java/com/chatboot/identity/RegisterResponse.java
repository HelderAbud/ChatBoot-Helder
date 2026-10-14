package com.chatboot.identity;

import java.util.UUID;

public record RegisterResponse(UUID userId, UUID workspaceId) {}
