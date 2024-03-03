package org.example.models.tasks;

import java.util.UUID;

public record TaskCompletedEvent(UUID taskId, UUID completedByUserId) {
    public static final String TOPIC = "task_completed";
}
