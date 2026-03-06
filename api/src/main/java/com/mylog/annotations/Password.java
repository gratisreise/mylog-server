package com.mylog.annotations;

import com.mylog.annotations.classes.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "{valid.password}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}