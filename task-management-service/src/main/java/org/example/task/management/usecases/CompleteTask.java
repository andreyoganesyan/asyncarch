package org.example.task.management.usecases;

import lombok.RequiredArgsConstructor;
import org.example.auth.provider.UserPrincipal;
import org.example.models.tasks.TaskCompletedEventV1;
import org.example.models.tasks.TaskTopics;
import org.example.task.management.db.TaskEntity;
import org.example.task.management.db.TaskEntityDto;
import org.example.task.management.db.TaskEntityRepository;
import org.example.task.management.util.HttpExceptionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CompleteTask {
    private final TaskEntityRepository taskEntityRepository;
    private final KafkaTemplate<UUID, TaskCompletedEventV1> kafkaTemplate;

    @PostMapping("/task/{id}/complete")
    @Transactional
    public TaskEntityDto completeTask(@PathVariable("id") UUID taskId) {
        TaskEntity task = taskEntityRepository.findById(taskId)
                .orElseThrow(() -> HttpExceptionUtil.create(HttpStatus.NOT_FOUND));

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserPrincipal user) || !task.getAssignee().getId().equals(user.id())) {
            throw HttpExceptionUtil.create(HttpStatus.FORBIDDEN);
        }

        if (task.getStatus() == TaskEntity.Status.COMPLETED) {
            throw HttpExceptionUtil.create(HttpStatus.BAD_REQUEST);
        }

        task.setStatus(TaskEntity.Status.COMPLETED);
        var savedTask = taskEntityRepository.save(task);
        kafkaTemplate.send(
                TaskTopics.TASK_COMPLETED,
                savedTask.getId(),
                new TaskCompletedEventV1()
                        .withTaskId(savedTask.getId())
                        .withCompletedByUserId(savedTask.getAssignee().getId())
                        .withTimestamp(Instant.now())
        );

        return new TaskEntityDto(savedTask.getId(), savedTask.getDescription(), savedTask.getStatus(), savedTask.getAssignee().getId());
    }
}
