package org.example.accounting.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.auth.provider.UserPrincipal;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user")
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserPrincipal.Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

}