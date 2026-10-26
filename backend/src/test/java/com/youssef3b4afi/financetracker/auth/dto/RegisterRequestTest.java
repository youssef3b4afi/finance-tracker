package com.youssef3b4afi.financetracker.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test of the registration rules themselves: runs the same Bean Validation engine that @Valid uses,
 * without Spring or a database, so each case takes microseconds. @ParameterizedTest runs the method
 * once per value in @ValueSource.
 */
class RegisterRequestTest {

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_PASSWORD = "secret-123";

    static ValidatorFactory factory;
    static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last+tag@mail.example.co.uk",
            "user@my-domain.io",
            "user@guc.edu.eg",
            "  padded@example.com  "})
    void acceptsRealEmailAddresses(String email) {
        assertThat(invalidFields(email, VALID_PASSWORD)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@g",              // no top-level domain: the reported bug
            "user@localhost",
            "user@example.c",      // top-level domain too short
            "user@example.123",    // numeric top-level domain
            "user@-example.com",   // label starts with a hyphen
            "user@example-.com",   // label ends with a hyphen
            "user@example..com",   // empty label
            "user@exam_ple.com",   // underscore isn't allowed in a domain
            "user@.com",
            "@example.com",
            "user.example.com"})
    void rejectsEmailWithoutProperDomain(String email) {
        assertThat(invalidFields(email, VALID_PASSWORD)).containsExactly("email");
    }

    @ParameterizedTest
    @ValueSource(strings = {"secret-123", "P@ssw0rd", "1234567!", "café#2026", "long passphrase 1!"})
    void acceptsPasswordWithDigitAndSpecialCharacter(String password) {
        assertThat(invalidFields(VALID_EMAIL, password)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "secret123",     // no special character
            "secret-abc",    // no digit
            "12345678",      // digits only
            "secret 123",    // a space doesn't count as a special character
            "càfé2026",      // accented letters don't count either
            "s-1"})          // too short, even though it has both
    void rejectsPasswordWithoutDigitAndSpecialCharacter(String password) {
        assertThat(invalidFields(VALID_EMAIL, password)).containsExactly("password");
    }

    /** Names of the fields that failed validation (each listed once, however many rules it broke). */
    private static Set<String> invalidFields(String email, String password) {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(new RegisterRequest(email, password));
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}
