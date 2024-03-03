package org.example.task.management.usecases;

import lombok.RequiredArgsConstructor;
import org.example.models.auth.User;
import org.example.task.management.db.TaskEntityDto;
import org.example.task.management.db.TaskEntityRepository;
import org.example.task.management.util.HttpExceptionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ViewAssignedTasks {
    private final TaskEntityRepository taskEntityRepository;

    @GetMapping("/task/")
    public List<TaskEntityDto> getAssignedTasks() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            throw HttpExceptionUtil.create(HttpStatus.UNAUTHORIZED);
        }

        return taskEntityRepository.findByAssigneeId(user.id())
                .stream()
                .map(task -> new TaskEntityDto(task.getId(), task.getDescription(), task.getStatus(), user.id()))
                .toList();
    }
}
