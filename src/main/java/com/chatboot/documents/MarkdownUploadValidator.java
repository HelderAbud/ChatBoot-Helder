package com.chatboot.documents;

import java.nio.charset.StandardCharsets;

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
        if (filename.length() > 500) {
            throw new UploadRejectedException(UploadRejectedException.Reason.FILENAME_TOO_LONG);
        }
        for (byte value : content) {
            if (value == 0) {
                throw new UploadRejectedException(UploadRejectedException.Reason.NULL_BYTE);
            }
        }
        String markdown = new String(content, StandardCharsets.UTF_8);
        for (MarkdownChunk chunk : new MarkdownChunker().chunk(markdown)) {
            if (chunk.heading().length() > 500) {
                throw new UploadRejectedException(UploadRejectedException.Reason.HEADING_TOO_LONG);
            }
        }
    }
}
