package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    // === Тесты isValidUsername ===

    @Test
    void testValidUsername() {
        assertTrue(ValidationUtils.isValidUsername("john_doe"));
        assertTrue(ValidationUtils.isValidUsername("User123"));
        assertTrue(ValidationUtils.isValidUsername("a_b_c"));
    }

    @Test
    void testInvalidUsername() {
        assertFalse(ValidationUtils.isValidUsername("ab")); // слишком короткое
        assertFalse(ValidationUtils.isValidUsername("a".repeat(21))); // слишком длинное
        assertFalse(ValidationUtils.isValidUsername("user@name")); // спецсимвол
        assertFalse(ValidationUtils.isValidUsername("user name")); // пробел
        assertFalse(ValidationUtils.isValidUsername(""));
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    // === Тесты isValidEmail ===

    @Test
    void testValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertTrue(ValidationUtils.isValidEmail("test.user+tag@sub.domain.co.uk"));
        assertTrue(ValidationUtils.isValidEmail("a@b.co"));
    }

    @Test
    void testInvalidEmail() {
        assertFalse(ValidationUtils.isValidEmail("invalid"));
        assertFalse(ValidationUtils.isValidEmail("@example.com"));
        assertFalse(ValidationUtils.isValidEmail("user@"));
        assertFalse(ValidationUtils.isValidEmail("user@.com"));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    // === Тесты isValidDate ===

    @Test
    void testValidDate() {
        assertFalse(ValidationUtils.isValidDate("2026-03-20"));
        assertTrue(ValidationUtils.isValidDate("2026-03-20 15:30"));
        assertTrue(ValidationUtils.isValidDate("2026-03-20 15:30:45"));
    }

    @Test
    void testInvalidDate() {
        assertFalse(ValidationUtils.isValidDate("20-03-2026")); // неверный порядок
        assertFalse(ValidationUtils.isValidDate("2026/03/20")); // неверный разделитель
        assertFalse(ValidationUtils.isValidDate("not-a-date"));
        assertFalse(ValidationUtils.isValidDate(""));
        assertFalse(ValidationUtils.isValidDate(null));
    }

    // === Тесты normalizeString ===

    @Test
    void testNormalizeString() {
        assertEquals("Hello World", ValidationUtils.normalizeString("  Hello   World  "));
        assertEquals("Test", ValidationUtils.normalizeString("Test"));
        assertNull(ValidationUtils.normalizeString(null));
    }

    @Test
    void testNormalizeStringLower() {
        assertEquals("hello world", ValidationUtils.normalizeStringLower("  HELLO   World  "));
    }

    @Test
    void testNormalizeStringUpper() {
        assertEquals("HELLO WORLD", ValidationUtils.normalizeStringUpper("  hello   World  "));
    }

    // === Тесты requireNonEmpty ===

    @Test
    void testRequireNonEmptyValid() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("value", "field"));
    }

    @Test
    void testRequireNonEmptyEmpty() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("", "Username")
        );
        assertTrue(ex.getMessage().contains("Username"));
    }

    @Test
    void testRequireNonEmptyNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(null, "Email")
        );
        assertTrue(ex.getMessage().contains("Email"));
    }

    // === Тесты requireNonNull ===

    @Test
    void testRequireNonNullValid() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonNull(new Object(), "field"));
    }

    @Test
    void testRequireNonNullNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.requireNonNull(null, "Role")
        );
        assertTrue(ex.getMessage().contains("Role"));
    }
}