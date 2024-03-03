package org.example.models.tasks;

public record TaskCudEvent(EventType type, Task task) {
    public static final String TOPIC = "task_streaming";

    public enum EventType {
        CREATED, UPDATED, DELETED
    }
}
