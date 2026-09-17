package com.hms.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

/**
 * Terminal UI helper providing structured tables, interactive prompts, input sanitization, and pagination.
 */
public final class ConsoleUtil {

    private ConsoleUtil() {
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("  " + title.toUpperCase());
        System.out.println("================================================================================");
    }

    public static void printSubHeader(String subtitle) {
        System.out.println();
        System.out.println("--- " + subtitle + " ---");
    }

    public static void printDivider() {
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("[WARNING] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static String promptString(Scanner scanner, String prompt, boolean required) {
        while (true) {
            System.out.print(prompt + (required ? " *: " : ": "));
            if (!scanner.hasNextLine()) {
                return "";
            }
            String input = scanner.nextLine().trim();
            if (required && input.isEmpty()) {
                printError("This field is required. Please enter a valid value.");
                continue;
            }
            return input;
        }
    }

    public static String promptStringWithDefault(Scanner scanner, String prompt, String currentVal) {
        System.out.print(prompt + " [" + currentVal + "]: ");
        if (!scanner.hasNextLine()) {
            return currentVal;
        }
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentVal : input;
    }

    public static int promptInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " (" + min + "-" + max + "): ");
            if (!scanner.hasNextLine()) {
                return max; // default to exit/back option on stream closure
            }
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val < min || val > max) {
                    printError("Value must be between " + min + " and " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                printError("Please enter a valid whole number.");
            }
        }
    }

    public static double promptDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt + ": ");
            if (!scanner.hasNextLine()) {
                return min;
            }
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val < min || val > max) {
                    printError("Amount must be between " + min + " and " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                printError("Please enter a valid numeric amount (e.g. 500.00).");
            }
        }
    }

    public static LocalDate promptDate(Scanner scanner, String prompt, boolean allowPast) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            if (!scanner.hasNextLine()) {
                return LocalDate.now();
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                printError("Date cannot be empty.");
                continue;
            }
            if (!InputValidator.isValidDate(input)) {
                printError("Invalid date format. Expected YYYY-MM-DD (e.g., 2026-10-15).");
                continue;
            }
            LocalDate parsed = DateTimeUtil.parseDate(input);
            if (!allowPast && parsed.isBefore(LocalDate.now())) {
                printError("Date cannot be in the past. Today is " + LocalDate.now() + ".");
                continue;
            }
            return parsed;
        }
    }

    public static LocalTime promptTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (HH:mm, 24-hr): ");
            if (!scanner.hasNextLine()) {
                return LocalTime.of(10, 0);
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                printError("Time cannot be empty.");
                continue;
            }
            if (!InputValidator.isValidTime(input)) {
                printError("Invalid time format. Expected 24-hr HH:mm (e.g., 10:30 or 14:00).");
                continue;
            }
            return DateTimeUtil.parseTime(input);
        }
    }

    /**
     * Renders a neatly padded ASCII table for terminal presentation.
     */
    public static void printTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return;

        int cols = headers.length;
        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) {
            widths[i] = headers[i].length();
        }

        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < Math.min(cols, row.length); i++) {
                    if (row[i] != null && row[i].length() > widths[i]) {
                        widths[i] = Math.min(row[i].length(), 35); // cap column width at 35 chars
                    }
                }
            }
        }

        StringBuilder lineSep = new StringBuilder("+");
        for (int w : widths) {
            for (int k = 0; k < w + 2; k++) lineSep.append("-");
            lineSep.append("+");
        }

        System.out.println(lineSep);

        // Print header
        System.out.print("|");
        for (int i = 0; i < cols; i++) {
            System.out.printf(" %-" + widths[i] + "s |", truncate(headers[i], widths[i]));
        }
        System.out.println();
        System.out.println(lineSep);

        // Print rows
        if (rows == null || rows.isEmpty()) {
            System.out.println("| " + center("(No records found)", lineSep.length() - 4) + " |");
        } else {
            for (String[] row : rows) {
                System.out.print("|");
                for (int i = 0; i < cols; i++) {
                    String val = (i < row.length && row[i] != null) ? row[i] : "";
                    System.out.printf(" %-" + widths[i] + "s |", truncate(val, widths[i]));
                }
                System.out.println();
            }
        }

        System.out.println(lineSep);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }

    private static String center(String text, int width) {
        if (text.length() >= width) return text;
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    /**
     * Pauses the terminal until the user presses Enter.
     */
    public static void pause(Scanner scanner) {
        System.out.println();
        System.out.print("Press [Enter] to continue...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
