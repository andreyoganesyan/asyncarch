package org.example.accounting.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "task")
public class TaskEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "description")
    private String description;

    @Column(name = "assignment_price", nullable = false)
    private Integer assignmentPrice;

    @Column(name = "completion_price", nullable = false)
    private Integer completionPrice;

}