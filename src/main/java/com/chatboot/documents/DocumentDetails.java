package com.chatboot.documents;

import java.util.UUID;

public record DocumentDetails(UUID documentId, String originalName, String status, String failureMessage) {}
