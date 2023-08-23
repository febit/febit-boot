package org.febit.boot.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdsValidator implements ConstraintValidator<Ids, Object[]> {

    private int min;
    private int max;

    @Override
    public void initialize(Ids ids) {
        this.min = ids.min();
        this.max = ids.max();
    }

    @Override
    public boolean isValid(Object[] ids, ConstraintValidatorContext context) {
        if (ids == null) {
            return false;
        }
        if (ids.length < min) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Size of ids should not less than " + min
                    )
                    .addConstraintViolation();
            return false;
        }
        if (ids.length > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Size of ids should not greater than " + max
                    )
                    .addConstraintViolation();
            return false;
        }
        for (var id : ids) {
            if (id != null) {
                continue;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Ids should not contains null elements"
                    )
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
