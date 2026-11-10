package com.chatboot.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.io.FileSystemResource;

class OllamaReadTimeoutTest {

    @Test
    void staysOpenLongerThanTheMeasuredGeneration() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("src/main/resources/application.yml"));
        Properties properties = yaml.getObject();
        Duration read = DurationStyle.detectAndParse(properties.getProperty("chatboot.ollama.read-timeout"));

        assertEquals(Duration.ofSeconds(180), read);
    }
}
