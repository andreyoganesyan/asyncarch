package org.example.accounting.listeners;

import lombok.RequiredArgsConstructor;
import org.example.accounting.TaskPriceGenerator;
import org.example.accounting.commands.ApplyTransactionCommand;
import org.example.accounting.db.*;
import org.example.models.accounting.TaskPriceAssignedEventV1;
import org.example.models.accounting.TaskPriceTopics;
import org.example.models.tasks.TaskCompletedEventV1;
import org.example.models.tasks.TaskTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskCompletedListener {
    private final ApplyTransactionCommand applyTransactionCommand;
    private final UserEntityRepository userEntityRepository;
    private final TaskEntityRepository taskEntityRepository;
    private final TaskPriceGenerator taskPriceGenerator;
    private final KafkaTemplate<UUID, TaskPriceAssignedEventV1> kafkaTemplate;

    @KafkaListener(topics = TaskTopics.TASK_COMPLETED, groupId = "accounting-service-task-completed")
    @RetryableTopic(retryTopicSuffix = "accounting-service-retry", dltTopicSuffix = "accounting-service-dlt")
    @Transactional
    public void accept(@Payload TaskCompletedEventV1 taskCompletedEvent) {
        UUID completedByUserId = taskCompletedEvent.getCompletedByUserId();
        UserEntity user = userEntityRepository.findById(completedByUserId)
                .orElseThrow(() -> new IllegalStateException("User with id %s not initialized".formatted(completedByUserId)));
        TaskEntity task = getOrInitializeTask(taskCompletedEvent.getTaskId());
        applyTransactionCommand.apply(
                user.getAccountId(),
                Optional.ofNullable(task.getDescription()).map(desc -> desc + " (completion)")
                        .orElse("Unknown task completion"),
                PaymentTransactionEntity.Type.INTERNAL_BALANCE_CHANGE,
                task.getCompletionPrice(),
                0,
                taskCompletedEvent.getTimestamp()
        );
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
