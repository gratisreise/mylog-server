package com.mylog.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$");

  private int minLength;
  private int maxLength;

  @Override
  public void initialize(Password constraintAnnotation) {
    this.minLength = constraintAnnotation.minLength();
    this.maxLength = constraintAnnotation.maxLength();
  }

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
      return true;
    }

    if (password.length() < minLength || password.length() > maxLength) {
      return false;
    }

    return PASSWORD_PATTERN.matcher(password).matches();
  }
}
