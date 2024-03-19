package org.example.accounting.listeners;

import lombok.RequiredArgsConstructor;
import org.example.accounting.TaskPriceGenerator;
import org.example.accounting.commands.ApplyTransactionCommand;
import org.example.accounting.db.*;
import org.example.models.accounting.TaskPriceAssignedEventV1;
import org.example.models.accounting.TaskPriceTopics;
import org.example.models.tasks.TaskAssignedEventV1;
import org.example.models.tasks.TaskCreatedEventV2;
import org.example.models.tasks.TaskTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAssignedListener {
    private final ApplyTransactionCommand applyTransactionCommand;
    private final UserEntityRepository userEntityRepository;
    private final TaskEntityRepository taskEntityRepository;
    private final TaskPriceGenerator taskPriceGenerator;
    private final KafkaTemplate<UUID, TaskPriceAssignedEventV1> kafkaTemplate;

    @KafkaListener(topics = TaskTopics.TASK_ASSIGNED, groupId = "accounting-service-task-assigned")
    @RetryableTopic(retryTopicSuffix = "accounting-service-retry", dltTopicSuffix = "accounting-service-dlt")
    @Transactional
    public void accept(@Payload TaskAssignedEventV1 taskAssignedEvent) {
        chargeForAssignment(
                taskAssignedEvent.getTaskId(),
                taskAssignedEvent.getAssigneeId(),
                taskAssignedEvent.getTimestamp());
    }

    @KafkaListener(topics = TaskTopics.TASK_CREATED, groupId = "accounting-service-task-created")
    @RetryableTopic(retryTopicSuffix = "accounting-service-retry", dltTopicSuffix = "accounting-service-dlt")
    @Transactional
    public void accept(@Payload TaskCreatedEventV2 taskCreatedEvent) {
        chargeForAssignment(
                taskCreatedEvent.getTaskId(),
                taskCreatedEvent.getAssigneeId(),
                taskCreatedEvent.getTimestamp());
    }

    private void chargeForAssignment(UUID taskId, UUID assigneeId, Instant timestamp) {
        UserEntity user = userEntityRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalStateException("User with id %s not initialized".formatted(assigneeId)));
        TaskEntity task = getOrInitializeTask(taskId);
        applyTransactionCommand.apply(
                user.getAccountId(),
                Optional.ofNullable(task.getDescription()).map(desc -> desc + " (assignment)")
                        .orElse("Task assignment"),
                PaymentTransactionEntity.Type.INTERNAL_BALANCE_CHANGE,
                0,
                task.getAssignmentPrice(),
                timestamp);
    }


    private TaskEntity getOrInitializeTask(UUID taskId) {
        Optional<TaskEntity> task = taskEntityRepository.findById(taskId);
        if (task.isPresent()) {
            return task.get();
        }
        var newTask = new TaskEntity();
        newTask.setId(taskId);
        newTask.setAssignmentPrice(taskPriceGenerator.generateTaskAssignmentPrice());
        newTask.setCompletionPrice(taskPriceGenerator.generateTaskCompletionPrice());
        TaskEntity savedTask = taskEntityRepository.save(newTask);
        kafkaTemplate.send(TaskPriceTopics.TASK_PRICE_ASSIGNED,
                savedTask.getId(),
                new TaskPriceAssignedEventV1()
                        .withTaskId(savedTask.getId())
                        .withAssignmentPrice(savedTask.getAssignmentPrice())
                        .withCompletionPrice(savedTask.getCompletionPrice()));
        return savedTask;
    }
}
