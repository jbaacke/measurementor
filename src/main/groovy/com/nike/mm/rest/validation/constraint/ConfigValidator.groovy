package com.nike.mm.rest.validation.constraint

import com.google.common.collect.Lists
import com.nike.mm.facade.impl.MeasureMentorRunFacade
import com.nike.mm.rest.validation.annotation.ValidConfig
import org.springframework.beans.factory.annotation.Autowired

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ConfigValidator implements ConstraintValidator<ValidConfig, Object> {

    public static final String CONFIGURATION_MANDATORY = "Configuration is mandatory"

    public static final String TYPE_FIELD_MANDATORY = "A field of name 'type' must be provided (GitHub, Jira, Jenkins, Stash)"

    @Autowired
    MeasureMentorRunFacade measureMentorRunFacade

    @Override
    void initialize(final ValidConfig constraintAnnotation) {

    }

    @Override
    boolean isValid(final Object value, final ConstraintValidatorContext context) {
        final List<String> errorMessages = Lists.newArrayList()
        if (value) {
            final boolean isCollection = isCollectionOrArray(value)
            if (isCollection) {
                value.each { final config ->
                    validateSingleConfig(config, errorMessages)
                }
            } else {
                validateSingleConfig(value, errorMessages)
            }
        } else {
            addViolation(CONFIGURATION_MANDATORY, errorMessages)
        }

        boolean isValid = true
        if (errorMessages.size()) {
            isValid = false
            context.disableDefaultConstraintViolation()
            errorMessages.each { final errorMessage ->
                context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation()
            }
        }
        return isValid
    }

    private static boolean isCollectionOrArray(final Object value) {
        [Collection, Object[]].any { it.isAssignableFrom(value.getClass()) }
    }

    private void validateSingleConfig(final Object config, final List<String> errorMessages) {
        final String errorMessage
        if (config.type) {
            errorMessage = this.measureMentorRunFacade.validateConfig(config)
        } else {
            errorMessage = TYPE_FIELD_MANDATORY
        }
        if (errorMessage) {
            addViolation(errorMessage, errorMessages)
        }
    }

    private static void addViolation(final String errorMessage, final List<String> errorMessages) {
        errorMessages.add(errorMessage)
    }
}