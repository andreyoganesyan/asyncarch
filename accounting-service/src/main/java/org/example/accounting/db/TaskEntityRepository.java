package org.example.accounting.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskEntityRepository extends JpaRepository<TaskEntity, UUID> {
}