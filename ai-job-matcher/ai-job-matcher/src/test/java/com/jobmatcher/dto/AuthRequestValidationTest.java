package com.jobmatcher.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private RegisterRequest register(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName(name);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String field) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void register_validRequest_hasNoViolations() {
        assertTrue(validator.validate(register("Test User", "test@example.com", "Passw0rd@")).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password1@",   // no uppercase
            "PASSWORD1@",   // no lowercase
            "Password@@",   // no digit
            "Password12",   // no special character
            "Pa1@",         // too short
    })
    void register_weakPassword_isRejected(String password) {
        var violations = validator.validate(register("Test User", "test@example.com", password));
        assertTrue(hasViolationOn(violations, "password"), "expected a password violation for: " + password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "missing-at.com", "@no-local.com"})
    void register_invalidEmail_isRejected(String email) {
        var violations = validator.validate(register("Test User", email, "Passw0rd@"));
        assertTrue(hasViolationOn(violations, "email"), "expected an email violation for: " + email);
    }

    @Test
    void register_nameTooShortOrBlank_isRejected() {
        assertTrue(hasViolationOn(validator.validate(register("A", "test@example.com", "Passw0rd@")), "fullName"));
        assertTrue(hasViolationOn(validator.validate(register("", "test@example.com", "Passw0rd@")), "fullName"));
    }

    @Test
    void login_blankPassword_isRejected() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("");

        assertTrue(hasViolationOn(validator.validate(request), "password"));
    }

    @Test
    void login_invalidEmail_isRejected() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nope");
        request.setPassword("Passw0rd@");

        assertTrue(hasViolationOn(validator.validate(request), "email"));
    }
}