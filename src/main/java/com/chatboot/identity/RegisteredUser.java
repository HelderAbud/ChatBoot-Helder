package com.chatboot.identity;

import java.util.UUID;

public record RegisteredUser(UUID userId, UUID workspaceId) {}
