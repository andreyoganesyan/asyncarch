package org.example.task.management;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.models.tasks.TaskAssignedEvent;
import org.example.models.tasks.TaskCompletedEvent;
import org.example.models.tasks.TaskCudEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfiguration {
    @Bean
    public NewTopic taskCudTopic() {
        return new NewTopic(TaskCudEvent.TOPIC, 1, (short) 1);
    }

    @Bean
    public NewTopic taskAssignedTopic() {
        return new NewTopic(TaskAssignedEvent.TOPIC, 1, (short) 1);
    }

    @Bean
    public NewTopic taskCompletedTopic() {
        return new NewTopic(TaskCompletedEvent.TOPIC, 1, (short) 1);
    }
}
