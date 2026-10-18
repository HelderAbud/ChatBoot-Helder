package com.chatboot.assistant;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OllamaClientConfig {

    @Bean
    RestClientCustomizer ollamaTimeouts(
            @Value("${chatboot.ollama.connect-timeout:5s}") Duration connect,
            @Value("${chatboot.ollama.read-timeout:120s}") Duration read) {
        return builder -> builder.requestFactory(OllamaClients.requestFactory(connect, read));
    }
}
