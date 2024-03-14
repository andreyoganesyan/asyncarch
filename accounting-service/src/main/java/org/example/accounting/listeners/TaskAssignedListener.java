package org.example.accounting.listeners;

import lombok.RequiredArgsConstructor;
import org.example.accounting.TaskPriceGenerator;
import org.example.accounting.commands.ApplyTransactionCommand;
import org.example.accounting.db.TaskEntity;
import org.example.accounting.db.TaskEntityRepository;
import org.example.accounting.db.UserEntity;
import org.example.accounting.db.UserEntityRepository;
import org.example.models.tasks.TaskAssignedEventV1;
import org.example.models.tasks.TaskTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAssignedListener {
    private final ApplyTransactionCommand applyTransactionCommand;
    private final UserEntityRepository userEntityRepository;
    private final TaskEntityRepository taskEntityRepository;
    private final TaskPriceGenerator taskPriceGenerator;

    @KafkaListener(topics = TaskTopics.TASK_ASSIGNED, groupId = "accounting-service-task-assigned")
    @RetryableTopic(retryTopicSuffix = "accounting-service-retry", dltTopicSuffix = "accounting-service-dlt")
    @Transactional
    public void accept(@Payload TaskAssignedEventV1 taskAssignedEvent) {
        UUID assigneeId = taskAssignedEvent.getAssigneeId();
        UserEntity user = userEntityRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalStateException("User with id %s not initialized".formatted(assigneeId)));
        TaskEntity task = getOrInitializeTask(taskAssignedEvent.getTaskId());
        applyTransactionCommand.apply(
                user.getAccountId(),
                Optional.ofNullable(task.getDescription()).map(desc -> desc + " (assignment)")
                        .orElse("Task assignment"),
                0,
                task.getAssignmentPrice(),
                taskAssignedEvent.getTimestamp()
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
        return taskEntityRepository.save(newTask);
    }
}
