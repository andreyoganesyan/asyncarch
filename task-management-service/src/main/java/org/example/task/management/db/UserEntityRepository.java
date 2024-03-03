package org.example.task.management.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {
    @Query("select u from UserEntity u where u.role = 'ROLE_EMPLOYEE' and u.isActive")
    List<UserEntity> findAllActiveEmployees();

}