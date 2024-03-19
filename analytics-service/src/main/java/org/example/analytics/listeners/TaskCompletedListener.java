package org.example.analytics.listeners;

import lombok.RequiredArgsConstructor;
import org.example.jooq.analytics.Tables;
import org.example.jooq.analytics.tables.records.TaskRecord;
import org.example.models.tasks.TaskCompletedEventV1;
import org.example.models.tasks.TaskTopics;
import org.jooq.DSLContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TaskCompletedListener {
    private final DSLContext dsl;

    @KafkaListener(topics = TaskTopics.TASK_COMPLETED, groupId = "analytics-service-task-completed")
    @Transactional
    public void accept(TaskCompletedEventV1 taskCompletedEvent) {
        var record = new TaskRecord();
        record.setId(taskCompletedEvent.getTaskId());
        record.setCompletionDate(LocalDate.ofInstant(taskCompletedEvent.getTimestamp(), ZoneId.of("UTC")));
        dsl.attach(record);
        record.merge(Tables.TASK.ID, Tables.TASK.COMPLETION_DATE);
    }
}
