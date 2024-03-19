package org.example.task.management.usecases;

import lombok.RequiredArgsConstructor;
import org.example.models.tasks.*;
import org.example.task.management.db.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CreateTask {

    private final UserEntityRepository userEntityRepository;
    private final TaskEntityRepository taskEntityRepository;
    private final KafkaTemplate<UUID, Object> kafkaTemplate;

    @PostMapping("/task")
    public TaskEntityDto createNewTask(@RequestBody TaskV1 taskToCreate) {
        var newTask = new TaskEntity();
        newTask.setId(Optional.ofNullable(taskToCreate.getId()).orElse(UUID.randomUUID()));
        newTask.setDescription(taskToCreate.getDescription());
        newTask.setAssignee(getRandomAssignee());

        var savedTask = taskEntityRepository.save(newTask);
        kafkaTemplate.send(TaskTopics.TASK_STREAMING,
                savedTask.getId(),
                new TaskCreatedEventV1()
                        .withTask(new TaskV1()
                                .withId(savedTask.getId())
                                .withDescription(savedTask.getDescription())));
        kafkaTemplate.send(TaskTopics.TASK_CREATED,
                savedTask.getId(),
                new TaskCreatedEventV2()
                        .withTaskId(savedTask.getId())
                        .withAssigneeId(savedTask.getAssignee().getId())
                        .withTimestamp(Instant.now()));

        return new TaskEntityDto(
                savedTask.getId(),
                savedTask.getDescription(),
                savedTask.getStatus(),
                savedTask.getAssignee().getId());
    }

    private UserEntity getRandomAssignee() {
        List<UserEntity> employees = userEntityRepository.findAllActiveEmployees();
        int employeeIndex = ThreadLocalRandom.current().nextInt(employees.size());
        return employees.get(employeeIndex);
    }
}
