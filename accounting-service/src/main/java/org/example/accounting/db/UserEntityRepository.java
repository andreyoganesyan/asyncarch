package org.example.accounting.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {
    @Query("select u from UserEntity u where u.accountId = ?1")
    Optional<UserEntity> findByAccountId(UUID accountId);

}