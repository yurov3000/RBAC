package org.example;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FormatUtils {

    // Символы для оформления таблиц
    private static final char BORDER_HORIZONTAL = '-';
    private static final char BORDER_VERTICAL = '|';
    private static final char BORDER_CORNER = '+';
    private static final char BORDER_TEE = '|';

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "Пустая таблица";
        }

        // Вычисляем ширину каждой колонки
        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }

        // Учитываем длину данных в строках
        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, colWidths.length); i++) {
                if (row[i] != null) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        // Верхняя граница
        sb.append(formatBorder(colWidths)).append("\n");

        // Заголовки
        sb.append(BORDER_VERTICAL);
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(padRight(headers[i], colWidths[i])).append(" ").append(BORDER_VERTICAL);
        }
        sb.append("\n");

        // Разделитель после заголовков
        sb.append(formatBorder(colWidths)).append("\n");

        // Строки данных
        for (String[] row : rows) {
            sb.append(BORDER_VERTICAL);
            for (int i = 0; i < headers.length; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                sb.append(" ").append(padRight(cell, colWidths[i])).append(" ").append(BORDER_VERTICAL);
            }
            sb.append("\n");
        }

        // Нижняя граница
        sb.append(formatBorder(colWidths));

        return sb.toString();
    }

    private static String formatBorder(int[] colWidths) {
        StringBuilder sb = new StringBuilder();
        sb.append(BORDER_CORNER);
        for (int width : colWidths) {
            sb.append(String.valueOf(BORDER_HORIZONTAL).repeat(width + 2));
            sb.append(BORDER_CORNER);
        }
        return sb.toString();
    }

    public static String formatBox(String text) {
        if (text == null || text.isEmpty()) {
            return "+--+\n|  |\n+--+";
        }

        String[] lines = text.split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, line.length());
        }

        StringBuilder sb = new StringBuilder();

        // Верхняя рамка
        sb.append("+").append("-".repeat(maxWidth + 2)).append("+\n");

        // Строки текста
        for (String line : lines) {
            sb.append("| ").append(padRight(line, maxWidth)).append(" |\n");
        }

        // Нижняя рамка
        sb.append("+").append("-".repeat(maxWidth + 2)).append("+");

        return sb.toString();
    }

    public static String formatHeader(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        int width = Math.max(text.length() + 4, 40);
        String border = "=".repeat(width);
        return String.format("%n%s%n  %s%n%s%n", border, text, border);
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength <= 3) return text.length() <= maxLength ? text : text.substring(0, maxLength);
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return " ".repeat(length - text.length()) + text;
    }

    public static String padCenter(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        int padding = length - text.length();
        int left = padding / 2;
        int right = padding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    public static String formatList(List<String> items, String prefix) {
        if (items == null || items.isEmpty()) {
            return "Пустой список";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (String item : items) {
            sb.append(String.format("%s%d. %s%n", prefix, i++, item != null ? item : ""));
        }
        return sb.toString().trim();
    }

    public static String formatKeyValue(String key, String value, int keyWidth) {
        return String.format("%s: %s", padRight(key, keyWidth), value != null ? value : "—");
    }
}