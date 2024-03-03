package org.example.task.management.db;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link TaskEntity}
 */
public record TaskEntityDto(UUID id, String description, TaskEntity.Status status,
                            UUID assigneeId) implements Serializable {
}