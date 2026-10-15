package com.chatboot.assistant;

public record ScoredChunk(String id, String documentName, String heading, String body, double score) {}
