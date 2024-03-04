package org.example.task.management.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaskEntityRepository extends JpaRepository<TaskEntity, UUID> {
    @Query("select t from TaskEntity t where t.status = 'TO_DO'")
    List<TaskEntity> findAllOpenTasks();

    @Query("select t from TaskEntity t where t.assignee.id = ?1")
    List<TaskEntity> findByAssigneeId(UUID id);


}