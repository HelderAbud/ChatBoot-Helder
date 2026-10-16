package com.chatboot.assistant;

public class EmbeddingUnavailableException extends RuntimeException {
    public EmbeddingUnavailableException() {}

    public EmbeddingUnavailableException(Throwable cause) {
        super(cause);
    }
}
