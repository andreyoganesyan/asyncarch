package org.example.accounting.listeners;

import lombok.RequiredArgsConstructor;
import org.example.accounting.TaskPriceGenerator;
import org.example.accounting.db.TaskEntity;
import org.example.accounting.db.TaskEntityRepository;
import org.example.models.tasks.TaskCreatedEventV1;
import org.example.models.tasks.TaskTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskStreamingListener {
    private final TaskEntityRepository taskEntityRepository;
    private final TaskPriceGenerator taskPriceGenerator;

    @KafkaListener(topics = TaskTopics.TASK_STREAMING, groupId = "accounting-service-task-streaming")
    public void acceptTaskCreatedEvent(@Payload TaskCreatedEventV1 taskCreatedEvent) {
        TaskEntity taskEntity = taskEntityRepository.findById(taskCreatedEvent.getTask().getId()).orElse(new TaskEntity());
        taskEntity.setId(taskCreatedEvent.getTask().getId());
        taskEntity.setDescription(taskCreatedEvent.getTask().getDescription());
        taskEntity.setAssignmentPrice(Optional.ofNullable(taskEntity.getAssignmentPrice())
                .orElse(taskPriceGenerator.generateTaskAssignmentPrice()));
        taskEntity.setCompletionPrice(Optional.ofNullable(taskEntity.getCompletionPrice())
                .orElse(taskPriceGenerator.generateTaskCompletionPrice()));
        taskEntityRepository.save(taskEntity);
    }
}
