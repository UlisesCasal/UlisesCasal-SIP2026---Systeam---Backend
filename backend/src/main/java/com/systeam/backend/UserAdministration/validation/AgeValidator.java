package com.systeam.backend.UserAdministration.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        // Dejamos que @NotNull valide si es null, el validador de edad asume válido si no hay fecha.
        if (dateOfBirth == null) {
            return true;
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= minAge;
    }
}
