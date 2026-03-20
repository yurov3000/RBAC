package org.example;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    // === Тесты truncate ===

    @Test
    void testTruncateShorterThanMax() {
        assertEquals("Hello", FormatUtils.truncate("Hello", 10));
    }

    @Test
    void testTruncateLongerThanMax() {
        assertEquals("Hello W...", FormatUtils.truncate("Hello World", 10));
    }

    @Test
    void testTruncateNull() {
        assertEquals("", FormatUtils.truncate(null, 10));
    }

    @Test
    void testTruncateMaxLessThan3() {
        assertEquals("He", FormatUtils.truncate("Hello", 2));
    }

    // === Тесты padRight ===

    @Test
    void testPadRightShorter() {
        assertEquals("Hi  ", FormatUtils.padRight("Hi", 4));
    }

    @Test
    void testPadRightEqual() {
        assertEquals("Hello", FormatUtils.padRight("Hello", 5));
    }

    @Test
    void testPadRightLonger() {
        assertEquals("Hello", FormatUtils.padRight("Hello", 3));
    }

    @Test
    void testPadRightNull() {
        assertEquals("    ", FormatUtils.padRight(null, 4));
    }

    // === Тесты padLeft ===

    @Test
    void testPadLeftShorter() {
        assertEquals("  Hi", FormatUtils.padLeft("Hi", 4));
    }

    @Test
    void testPadLeftNull() {
        assertEquals("    ", FormatUtils.padLeft(null, 4));
    }

    // === Тесты padCenter ===

    @Test
    void testPadCenterOddPadding() {
        assertEquals(" Hello ", FormatUtils.padCenter("Hello", 7));
    }

    @Test
    void testPadCenterEvenPadding() {
        assertEquals(" Hello  ", FormatUtils.padCenter("Hello", 8));
    }

    // === Тесты formatBox ===

    @Test
    void testFormatBoxSingleLine() {
        String result = FormatUtils.formatBox("Hello");
        assertTrue(result.contains("+-------+"));
        assertTrue(result.contains("| Hello |"));
    }

    @Test
    void testFormatBoxMultiLine() {
        String result = FormatUtils.formatBox("Line1\nLine2");
        assertTrue(result.contains("| Line1 |"));
        assertTrue(result.contains("| Line2 |"));
    }

    @Test
    void testFormatBoxEmpty() {
        String result = FormatUtils.formatBox("");
        assertTrue(result.contains("+--+"));
    }

    // === Тесты formatHeader ===

    @Test
    void testFormatHeader() {
        String result = FormatUtils.formatHeader("Title");
        assertTrue(result.contains("Title"));
        assertTrue(result.contains("===="));
    }

    @Test
    void testFormatHeaderEmpty() {
        assertEquals("", FormatUtils.formatHeader(""));
    }

    // === Тесты formatTable ===

    @Test
    void testFormatTableBasic() {
        String[] headers = {"Name", "Age"};
        List<String[]> rows = List.of(
                new String[]{"Alice", "30"},
                new String[]{"Bob", "25"}
        );
        String table = FormatUtils.formatTable(headers, rows);

        assertTrue(table.contains("+"));
        assertTrue(table.contains("|"));
        assertTrue(table.contains("Alice"));
        assertTrue(table.contains("Bob"));
    }

    @Test
    void testFormatTableEmptyRows() {
        String[] headers = {"Col1", "Col2"};
        String table = FormatUtils.formatTable(headers, List.of());

        assertTrue(table.contains("Col1"));
        assertTrue(table.contains("Col2"));
    }

    @Test
    void testFormatTableNullHeaders() {
        assertEquals("Пустая таблица", FormatUtils.formatTable(null, List.of()));
    }

    // === Тесты formatList ===

    @Test
    void testFormatList() {
        List<String> items = List.of("First", "Second", "Third");
        String result = FormatUtils.formatList(items, "  ");

        assertTrue(result.contains("1. First"));
        assertTrue(result.contains("2. Second"));
        assertTrue(result.contains("3. Third"));
    }

    @Test
    void testFormatListEmpty() {
        assertEquals("Пустой список", FormatUtils.formatList(List.of(), ""));
    }

    // === Тесты formatKeyValue ===

    @Test
    void testFormatKeyValue() {
        String result = FormatUtils.formatKeyValue("Name", "Alice", 10);
        assertEquals("Name      : Alice", result);
    }

    @Test
    void testFormatKeyValueNullValue() {
        String result = FormatUtils.formatKeyValue("Email", null, 10);
        assertEquals("Email     : —", result);
    }
}