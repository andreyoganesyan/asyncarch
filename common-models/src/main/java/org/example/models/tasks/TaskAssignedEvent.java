package org.example.models.tasks;

import java.util.UUID;

public record TaskAssignedEvent(UUID taskId, UUID assigneeId) {
    public static final String TOPIC = "task_assigned";
}
