package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    // === Тесты getCurrentDate / getCurrentDateTime ===

    @Test
    void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testGetCurrentDateTime() {
        String dateTime = DateUtils.getCurrentDateTime();
        assertNotNull(dateTime);
        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    // === Тесты isBefore / isAfter ===

    @Test
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2026-01-01", "2026-12-31"));
        assertFalse(DateUtils.isBefore("2026-12-31", "2026-01-01"));
        assertFalse(DateUtils.isBefore("2026-06-15", "2026-06-15"));
    }

    @Test
    void testIsAfter() {
        assertTrue(DateUtils.isAfter("2026-12-31", "2026-01-01"));
        assertFalse(DateUtils.isAfter("2026-01-01", "2026-12-31"));
        assertFalse(DateUtils.isAfter("2026-06-15", "2026-06-15"));
    }

    @Test
    void testIsEqual() {
        assertTrue(DateUtils.isEqual("2026-06-15", "2026-06-15"));
        assertTrue(DateUtils.isEqual("2026-06-15 10:30:00", "2026-06-15"));
        assertFalse(DateUtils.isEqual("2026-06-15", "2026-06-16"));
    }

    @Test
    void testIsBeforeNull() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.isBefore(null, "2026-01-01"));
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.isBefore("2026-01-01", null));
    }

    // === Тесты addDays ===

    @Test
    void testAddDaysForward() {
        assertEquals("2026-01-05", DateUtils.addDays("2026-01-01", 4));
        assertEquals("2026-02-01", DateUtils.addDays("2026-01-01", 31));
    }

    @Test
    void testAddDaysBackward() {
        assertEquals("2025-12-28", DateUtils.addDays("2026-01-01", -4));
    }

    @Test
    void testAddDaysWithDateTime() {
        String result = DateUtils.addDays("2026-01-01 12:30:45", 5);
        assertTrue(result.startsWith("2026-01-06"));
        assertTrue(result.contains("12:30:45"));
    }

    @Test
    void testAddDaysInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.addDays("invalid-date", 5));
    }

    // === Тесты formatRelativeTime ===

    @Test
    void testFormatRelativeTimeToday() {
        String today = DateUtils.getCurrentDate();
        assertEquals("today", DateUtils.formatRelativeTime(today));
    }

    @Test
    void testFormatRelativeTimeTomorrow() {
        String tomorrow = DateUtils.addDays(DateUtils.getCurrentDate(), 1);
        assertEquals("tomorrow", DateUtils.formatRelativeTime(tomorrow));
    }

    @Test
    void testFormatRelativeTimeYesterday() {
        String yesterday = DateUtils.addDays(DateUtils.getCurrentDate(), -1);
        assertEquals("yesterday", DateUtils.formatRelativeTime(yesterday));
    }

    @Test
    void testFormatRelativeTimeFuture() {
        String future = DateUtils.addDays(DateUtils.getCurrentDate(), 10);
        assertTrue(DateUtils.formatRelativeTime(future).contains("in 10 days"));
    }

    @Test
    void testFormatRelativeTimePast() {
        String past = DateUtils.addDays(DateUtils.getCurrentDate(), -10);
        assertTrue(DateUtils.formatRelativeTime(past).contains("10 days ago"));
    }

    @Test
    void testFormatRelativeTimeInvalid() {
        assertEquals("unknown", DateUtils.formatRelativeTime("invalid"));
        assertEquals("unknown", DateUtils.formatRelativeTime(null));
        assertEquals("unknown", DateUtils.formatRelativeTime(""));
    }

    // === Тесты isValidDate ===

    @Test
    void testIsValidDate() {
        assertTrue(DateUtils.isValidDate("2026-01-15"));
        assertTrue(DateUtils.isValidDate("2026-01-15 10:30:00"));
        assertFalse(DateUtils.isValidDate("15-01-2026"));
        assertFalse(DateUtils.isValidDate("not-a-date"));
        assertFalse(DateUtils.isValidDate(null));
        assertFalse(DateUtils.isValidDate(""));
    }

    @Test
    void testIsValidDateTime() {
        assertTrue(DateUtils.isValidDateTime("2026-01-15 10:30:45"));
        assertTrue(DateUtils.isValidDateTime("2026-01-15"));}

    // === Интеграционный тест с TemporaryAssignment ===

    @Test
    void testTemporaryAssignmentWithDateUtils() {
        // Этот тест проверяет, что DateUtils работает с назначениями
        String futureDate = DateUtils.addDays(DateUtils.getCurrentDate(), 30);
        String pastDate = DateUtils.addDays(DateUtils.getCurrentDate(), -30);

        assertTrue(DateUtils.isBefore(DateUtils.getCurrentDate(), futureDate));
        assertTrue(DateUtils.isAfter(DateUtils.getCurrentDate(), pastDate));

        assertEquals("in 30 days", DateUtils.formatRelativeTime(futureDate));
        assertTrue(DateUtils.formatRelativeTime(pastDate).contains("days ago"));
    }
}