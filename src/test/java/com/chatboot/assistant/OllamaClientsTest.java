package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

class OllamaClientsTest {

    @Test
    void limitsHowLongTheModelCallCanWait() throws Exception {
        ClientHttpRequestFactory factory = OllamaClients.requestFactory(Duration.ofSeconds(5), Duration.ofSeconds(120));
        SimpleClientHttpRequestFactory simple = (SimpleClientHttpRequestFactory) factory;

        assertEquals(5_000, timeout(simple, "connectTimeout"));
        assertEquals(120_000, timeout(simple, "readTimeout"));
    }

    private static int timeout(SimpleClientHttpRequestFactory factory, String fieldName) throws Exception {
        Field field = SimpleClientHttpRequestFactory.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(factory);
    }
}
