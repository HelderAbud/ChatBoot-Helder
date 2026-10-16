package com.chatboot.assistant;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Component
public class OllamaEmbeddingAdapter implements EmbeddingPort {
    private final EmbeddingModel embeddingModel;

    public OllamaEmbeddingAdapter(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] embed(String text) {
        try {
            return embeddingModel.embed(text);
        } catch (RuntimeException ex) {
            throw new EmbeddingUnavailableException(ex);
        }
    }
}
