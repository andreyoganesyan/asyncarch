package org.example.analytics.listeners;

import lombok.RequiredArgsConstructor;
import org.example.jooq.analytics.Tables;
import org.example.jooq.analytics.tables.records.TaskRecord;
import org.example.models.accounting.TaskPriceAssignedEventV1;
import org.example.models.accounting.TaskPriceTopics;
import org.jooq.DSLContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskPriceAssignedListener {
    private final DSLContext dsl;
    @KafkaListener(topics = TaskPriceTopics.TASK_PRICE_ASSIGNED, groupId = "analytics-service-task-price-assigned")
    @Transactional
    public void accept(@Payload TaskPriceAssignedEventV1 taskPriceAssignedEvent) {
        TaskRecord taskRecord = new TaskRecord();
        taskRecord.setId(taskPriceAssignedEvent.getTaskId());
        taskRecord.setAssignmentPrice(taskPriceAssignedEvent.getAssignmentPrice());
        taskRecord.setCompletionPrice(taskPriceAssignedEvent.getCompletionPrice());
        dsl.attach(taskRecord);
        taskRecord.merge(Tables.TASK.ID, Tables.TASK.ASSIGNMENT_PRICE, Tables.TASK.COMPLETION_PRICE);
    }
}
