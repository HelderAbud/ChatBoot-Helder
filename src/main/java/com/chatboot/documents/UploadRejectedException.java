package com.chatboot.documents;

public class UploadRejectedException extends RuntimeException {
    public enum Reason { EMPTY, TOO_LARGE, NOT_MARKDOWN, NULL_BYTE }

    private final Reason reason;

    public UploadRejectedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
