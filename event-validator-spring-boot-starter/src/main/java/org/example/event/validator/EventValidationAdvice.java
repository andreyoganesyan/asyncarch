package org.example.event.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Log4j2
@RequiredArgsConstructor
public class EventValidationAdvice {
    private final Validator validator;

    // this advice does not cover all possible ways to produce an event, but this is not production code
    @Before(value = "execution(* org.springframework.kafka.core.KafkaTemplate.send(..)) && args(topic,key,data))", argNames = "topic,key,data")
    public void validateEvent(String topic, Object key, Object data) {
        log.info("Running event validation for {}", data.getClass().getSimpleName());
        this.assertValid(data);
    }

    private <T> void assertValid(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid %s event, violations: %s".formatted(
                            object.getClass().getSimpleName(),
                            violations.stream()
                                    .map(violation -> "[%s] %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                                    .collect(Collectors.joining("\n"))));
        }
    }

}
