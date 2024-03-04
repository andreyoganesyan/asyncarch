package org.example.task.management.usecases;

import lombok.RequiredArgsConstructor;
import org.example.models.tasks.TaskAssignedEvent;
import org.example.task.management.db.TaskEntity;
import org.example.task.management.db.TaskEntityRepository;
import org.example.task.management.db.UserEntity;
import org.example.task.management.db.UserEntityRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShuffleTasks {

    private final UserEntityRepository userEntityRepository;
    private final TaskEntityRepository taskEntityRepository;
    private final KafkaTemplate<UUID, TaskAssignedEvent> kafkaTemplate;

    @PostMapping("/task/shuffle")
    public void shuffleOpenTasks() {
        List<TaskEntity> openTasks = taskEntityRepository.findAllOpenTasks();
        List<UserEntity> employees = userEntityRepository.findAllActiveEmployees();

        for (var task : openTasks) {
            int randomEmployeeIndex = ThreadLocalRandom.current().nextInt(employees.size());
            task.setAssignee(employees.get(randomEmployeeIndex));
        }
        List<TaskEntity> savedTasks = taskEntityRepository.saveAll(openTasks);
        for (var task : savedTasks) {
            kafkaTemplate.send(TaskAssignedEvent.TOPIC,
                    task.getId(),
                    new TaskAssignedEvent(task.getId(), task.getAssignee().getId())
            );
        }
    }
}
