package org.example.auth;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.models.auth.AuthTopics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    public NewTopic userStreamingTopic() {
        return new NewTopic(AuthTopics.USER_STREAMING, 1, (short) 1);
    }
}
