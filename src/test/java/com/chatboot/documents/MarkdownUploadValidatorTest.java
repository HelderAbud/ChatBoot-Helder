package com.chatboot.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MarkdownUploadValidatorTest {

    private final MarkdownUploadValidator validator = new MarkdownUploadValidator();

    @Test
    void acceptsMarkdownWithinTwoMegabytes() {
        validator.validate("manual.md", "# Titulo".getBytes());
    }

    @Test
    void rejectsEmptyFile() {
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.md", new byte[0]));
        assertEquals(UploadRejectedException.Reason.EMPTY, error.reason());
    }

    @Test
    void rejectsFileLargerThanTwoMegabytes() {
        byte[] content = new byte[2_097_153];
        content[0] = '#';
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.md", content));
        assertEquals(UploadRejectedException.Reason.TOO_LARGE, error.reason());
    }

    @Test
    void rejectsNonMarkdownExtension() {
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.pdf", "# Titulo".getBytes()));
        assertEquals(UploadRejectedException.Reason.NOT_MARKDOWN, error.reason());
    }

    @Test
    void rejectsNullByte() {
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.md", new byte[] {'#', 0}));
        assertEquals(UploadRejectedException.Reason.NULL_BYTE, error.reason());
    }

    @Test
    void rejectsANullByteInTheFileName() {
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual\0.md", "# Titulo".getBytes()));
        assertEquals(UploadRejectedException.Reason.NULL_BYTE, error.reason());
    }
}
