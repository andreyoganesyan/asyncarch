package org.example.auth.provider;

import jakarta.annotation.Nonnull;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

public record UserPrincipal(@Nonnull UUID id,
                            @Nonnull String email,
                            String displayName,
                            @Nonnull Role role) {
    public enum Role implements GrantedAuthority {
        ROLE_ADMIN, ROLE_ACCOUNTANT, ROLE_EMPLOYEE;

        @Override
        public String getAuthority() {
            return this.name();
        }
    }
}
