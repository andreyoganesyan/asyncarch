package org.example.analytics.listeners;

import lombok.RequiredArgsConstructor;
import org.example.jooq.analytics.Tables;
import org.example.jooq.analytics.tables.records.TaskRecord;
import org.example.models.tasks.TaskCreatedEventV1;
import org.example.models.tasks.TaskCudEventV1;
import org.example.models.tasks.TaskTopics;
import org.example.models.tasks.TaskV1;
import org.jooq.DSLContext;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@KafkaListener(topics = TaskTopics.TASK_STREAMING, groupId = "analytics-service-task-streaming")
public class TaskStreamingListener {
    private final DSLContext dsl;

    @KafkaHandler
    @Transactional
    public void acceptTaskCreatedEvent(@Payload TaskCreatedEventV1 taskCreatedEvent) {
        createOrUpdateTask(taskCreatedEvent.getTask());
    }

    @KafkaHandler
    @Transactional
    public void acceptTaskCudEvent(@Payload TaskCudEventV1 taskCudEvent) {
        switch (taskCudEvent.getEventType()) {
            case CREATED, UPDATED -> {
                createOrUpdateTask(taskCudEvent.getTask());
            }
        }
    }

    private void createOrUpdateTask(TaskV1 task) {
        TaskRecord taskRecord = new TaskRecord();
        taskRecord.setId(task.getId());
        taskRecord.setDescription(task.getDescription());
        dsl.attach(taskRecord);
        taskRecord.merge(Tables.TASK.ID, Tables.TASK.DESCRIPTION);
    }
}