package com.chatboot.documents;

public class MarkdownUploadValidator {
    static final int MAX_BYTES = 2_097_152;

    public void validate(String filename, byte[] content) {
        if (content == null || content.length == 0) {
            throw new UploadRejectedException(UploadRejectedException.Reason.EMPTY);
        }
        if (content.length > MAX_BYTES) {
            throw new UploadRejectedException(UploadRejectedException.Reason.TOO_LARGE);
        }
        if (filename == null || !filename.toLowerCase().endsWith(".md")) {
            throw new UploadRejectedException(UploadRejectedException.Reason.NOT_MARKDOWN);
        }
        if (filename.indexOf('\0') >= 0) {
            throw new UploadRejectedException(UploadRejectedException.Reason.NULL_BYTE);
        }
        for (byte value : content) {
            if (value == 0) {
                throw new UploadRejectedException(UploadRejectedException.Reason.NULL_BYTE);
            }
        }
    }
}
