package org.example.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.models.auth.User;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user", schema = "auth")
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private User.Role role;
}