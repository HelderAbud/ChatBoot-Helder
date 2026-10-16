package com.chatboot.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class UploadExceptionHandlerTest {

    @Test
    void explainsWhenTheMultipartLimitRejectsTheFile() {
        ResponseEntity<UploadRejection> response =
                new UploadExceptionHandler().tooLarge(new MaxUploadSizeExceededException(MarkdownUploadValidator.MAX_BYTES));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("TOO_LARGE", response.getBody().reason());
    }
}
