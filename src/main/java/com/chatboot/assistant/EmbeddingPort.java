package com.chatboot.assistant;

public interface EmbeddingPort {
    float[] embed(String text);
}
