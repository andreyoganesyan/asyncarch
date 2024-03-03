package org.example.task.management.usecases;

import lombok.RequiredArgsConstructor;
import org.example.models.tasks.Task;
import org.example.models.tasks.TaskAssignedEvent;
import org.example.models.tasks.TaskCudEvent;
import org.example.task.management.db.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public TaskEntityDto createNewTask(@RequestBody Task taskToCreate) {
        var newTask = new TaskEntity();
        newTask.setId(Optional.ofNullable(taskToCreate.id()).orElse(UUID.randomUUID()));
        newTask.setDescription(taskToCreate.description());
        newTask.setAssignee(getRandomAssignee());

        var savedTask = taskEntityRepository.save(newTask);
        kafkaTemplate.send(TaskCudEvent.TOPIC,
                savedTask.getId(),
                new TaskCudEvent(TaskCudEvent.EventType.CREATED, new Task(savedTask.getId(), savedTask.getDescription())));
        kafkaTemplate.send(TaskAssignedEvent.TOPIC,
                savedTask.getId(),
                new TaskAssignedEvent(savedTask.getId(), savedTask.getAssignee().getId()));

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
