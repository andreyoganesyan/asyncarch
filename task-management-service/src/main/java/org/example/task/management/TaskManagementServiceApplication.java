package org.example.task.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableMethodSecurity
@EnableKafka
@EnableKafkaRetryTopic
@EnableScheduling
public class TaskManagementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagementServiceApplication.class, args);
    }
}
