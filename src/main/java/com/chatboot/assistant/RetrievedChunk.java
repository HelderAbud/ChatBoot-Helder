package com.chatboot.assistant;

public record RetrievedChunk(String id, String documentName, String heading, double score) {}
