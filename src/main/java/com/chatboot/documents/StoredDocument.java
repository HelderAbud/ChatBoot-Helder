package com.chatboot.documents;

import java.util.UUID;

public record StoredDocument(UUID documentId, String status) {}
