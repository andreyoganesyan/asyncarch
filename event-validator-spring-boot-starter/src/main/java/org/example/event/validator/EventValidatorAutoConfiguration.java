package org.example.event.validator;

import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@AutoConfiguration
public class EventValidatorAutoConfiguration {

    @Bean
    public EventValidationAdvice eventValidationAdvice(Validator validator) {
        return new EventValidationAdvice(validator);
    }

    @Bean
    @ConditionalOnMissingBean(LocalValidatorFactoryBean.class)
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
