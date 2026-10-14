package com.chatboot.documents;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentConfig {
    @Bean
    MarkdownUploadValidator markdownUploadValidator() {
        return new MarkdownUploadValidator();
    }

    @Bean
    MarkdownChunker markdownChunker() {
        return new MarkdownChunker();
    }
}
