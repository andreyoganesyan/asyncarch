package org.example.analytics.queries;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.example.jooq.analytics.tables.records.TaskRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.example.jooq.analytics.Tables.TASK;

@Service
@RequiredArgsConstructor
public class MostExpensiveTaskQuery {
    private final DSLContext dsl;

    public QueryResult execute(LocalDate startDate, LocalDate endDate) {
        TaskRecord task = dsl.select()
                .from(TASK)
                .where(TASK.COMPLETION_DATE.between(startDate, endDate))
                .orderBy(TASK.COMPLETION_PRICE.desc())
                .limit(1)
                .fetchOneInto(TASK);

        var resultTask = Optional.ofNullable(task)
                .map(taskRecord -> new QueryResult.Task(
                        taskRecord.getId(),
                        taskRecord.getDescription(),
                        taskRecord.getAssignmentPrice(),
                        taskRecord.getCompletionPrice(),
                        taskRecord.getCompletionDate()
                )).orElse(null);
        return new QueryResult(resultTask);
    }

    public record QueryResult(@Nullable Task mostExpensiveTask) {
        public record Task(UUID taskId,
                           String description,
                           Integer assignmentPrice,
                           Integer completionPrice, LocalDate completionDate) {

        }
    }
}
