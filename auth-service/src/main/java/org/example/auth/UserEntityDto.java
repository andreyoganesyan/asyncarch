package org.example.auth;

import org.example.models.auth.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link UserEntity}
 */
public record UserEntityDto(UUID id, String email, String password, String displayName,
                            User.Role role) implements Serializable {
}