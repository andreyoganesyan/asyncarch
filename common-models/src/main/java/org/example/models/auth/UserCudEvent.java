package org.example.models.auth;

public record UserCudEvent(EventType eventType, User user) {
    public static final String TOPIC = "user_streaming";

    public enum EventType {
        CREATED, UPDATED, DELETED
    }
}
