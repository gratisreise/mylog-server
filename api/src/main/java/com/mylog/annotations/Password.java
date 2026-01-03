package com.mylog.annotations;

import com.mylog.annotations.classes.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class) // 검증 로직 클래스 지정
@Target({ElementType.FIELD, ElementType.PARAMETER}) // 필드와 파라미터에 적용
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "{valid.password}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}