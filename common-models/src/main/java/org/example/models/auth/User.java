package org.example.models.auth;

import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

public record User(UUID id, String email, String displayName, Role role) {
    public enum Role implements GrantedAuthority {
        ROLE_ADMIN, ROLE_ACCOUNTANT, ROLE_EMPLOYEE;

        @Override
        public String getAuthority() {
            return this.name();
        }
    }
}
