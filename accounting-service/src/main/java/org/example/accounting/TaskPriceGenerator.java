package org.example.accounting;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class TaskPriceGenerator {
    public Integer generateTaskAssignmentPrice() {
        return ThreadLocalRandom.current().nextInt(10, 20);
    }

    public Integer generateTaskCompletionPrice() {
        return ThreadLocalRandom.current().nextInt(20, 40);
    }
}
