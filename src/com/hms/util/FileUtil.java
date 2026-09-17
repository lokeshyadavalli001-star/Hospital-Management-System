package com.hms.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * File I/O and CSV serialization helper supporting RFC-4180 style quoting and directory initialization.
 */
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Ensures parent directories and the target file exist.
     */
    public static void ensureFileExists(String filePath, String defaultHeader) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
            if (defaultHeader != null && !defaultHeader.trim().isEmpty()) {
                Files.write(path, (defaultHeader + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * Splits a CSV line into tokens, respecting double-quoted values containing commas.
     */
    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    /**
     * Escapes a single string token for CSV output if it contains commas, quotes, or newlines.
     */
    public static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Joins multiple string values into a single valid CSV line.
     */
    public static String toCsvLine(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(escapeCsvField(fields.get(i)));
        }
        return sb.toString();
    }
}
