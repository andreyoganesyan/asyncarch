package org.example.auth;

import org.example.models.auth.UserV1;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link UserEntity}
 */
public record UserEntityDto(UUID id, String email, String password, String displayName,
                            UserEntity.Role role) implements Serializable {
}