package org.example.accounting.listeners;

import lombok.RequiredArgsConstructor;
import org.example.accounting.TaskPriceGenerator;
import org.example.accounting.db.TaskEntity;
import org.example.accounting.db.TaskEntityRepository;
import org.example.models.tasks.TaskCreatedEventV1;
import org.example.models.tasks.TaskCudEventV1;
import org.example.models.tasks.TaskTopics;
import org.example.models.tasks.TaskV1;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@KafkaListener(topics = TaskTopics.TASK_STREAMING, groupId = "accounting-service-task-streaming")
public class TaskStreamingListener {
    private final TaskEntityRepository taskEntityRepository;
    private final TaskPriceGenerator taskPriceGenerator;

    @KafkaHandler
    public void acceptTaskCreatedEvent(@Payload TaskCreatedEventV1 taskCreatedEvent) {
        createOrUpdateTask(taskCreatedEvent.getTask());
    }

    @KafkaHandler
    public void acceptTaskCudEvent(@Payload TaskCudEventV1 taskCudEvent) {
        switch (taskCudEvent.getEventType()) {
            case CREATED, UPDATED -> {
                createOrUpdateTask(taskCudEvent.getTask());
            }
        }
    }

    private void createOrUpdateTask(TaskV1 task) {
        TaskEntity taskEntity = taskEntityRepository.findById(task.getId()).orElse(new TaskEntity());
        taskEntity.setId(task.getId());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setAssignmentPrice(Optional.ofNullable(taskEntity.getAssignmentPrice())
                .orElse(taskPriceGenerator.generateTaskAssignmentPrice()));
        taskEntity.setCompletionPrice(Optional.ofNullable(taskEntity.getCompletionPrice())
                .orElse(taskPriceGenerator.generateTaskCompletionPrice()));
        taskEntityRepository.save(taskEntity);
    }
}
