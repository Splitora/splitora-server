package com.satwik.splitora.validator;

import com.satwik.splitora.persistence.entities.UnregisteredUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator to check if either email or phone number is provided for an UnregisteredUser.
 */
public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrPhoneRequired, UnregisteredUser> {

    @Override
    public boolean isValid(UnregisteredUser user, ConstraintValidatorContext context) {
        if (user == null) {
            return true; // Null is valid, handled by @NotNull if needed
        }
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isEmpty();
        boolean hasPhone = user.getCountryCode() != null && user.getPhoneNumber() > 0;
        return hasEmail || hasPhone;
    }
}