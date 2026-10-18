package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.chatboot.documents.DocumentService;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class ModelCallTransactionTest {

    @Test
    void questionDoesNotKeepATransactionOpenWhileTheModelRuns() throws Exception {
        Method ask = QuestionService.class.getMethod("ask", UUID.class, UUID.class, String.class, double.class);

        assertNull(ask.getAnnotation(Transactional.class));
    }

    @Test
    void uploadDoesNotKeepATransactionOpenWhileTheEmbeddingRuns() throws Exception {
        Method store = DocumentService.class.getMethod("store", UUID.class, String.class, byte[].class);

        assertNull(store.getAnnotation(Transactional.class));
    }
}
