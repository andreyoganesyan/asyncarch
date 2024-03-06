package org.example.task.management;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.models.tasks.TaskTopics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfiguration {
    @Bean
    public NewTopic taskCudTopic() {
        return new NewTopic(TaskTopics.TASK_STREAMING, 1, (short) 1);
    }

    @Bean
    public NewTopic taskAssignedTopic() {
        return new NewTopic(TaskTopics.TASK_ASSIGNED, 1, (short) 1);
    }

    @Bean
    public NewTopic taskCompletedTopic() {
        return new NewTopic(TaskTopics.TASK_COMPLETED, 1, (short) 1);
    }
}
