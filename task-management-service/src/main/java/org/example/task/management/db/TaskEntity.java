package org.example.task.management.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "task", schema = "tasks")
public class TaskEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.TO_DO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignee_id", nullable = false)
    private UserEntity assignee;

    public enum Status {
        TO_DO, COMPLETED
    }

}