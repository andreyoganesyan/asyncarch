package org.example.task.management.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.models.auth.User;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user", schema = "tasks")
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private User.Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}