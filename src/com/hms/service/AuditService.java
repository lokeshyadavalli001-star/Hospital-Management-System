package com.hms.service;

import com.hms.util.DateTimeUtil;
import com.hms.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit and security activity logger providing an append-only transaction trail.
 */
public class AuditService {

    private final String logFilePath;
    private final Object lock = new Object();

    public AuditService(String logFilePath) {
        this.logFilePath = logFilePath;
        try {
            FileUtil.ensureFileExists(logFilePath, "# HMS Audit Log Started: " + LocalDateTime.now());
        } catch (IOException e) {
            System.err.println("[WARN] Unable to initialize audit log file: " + e.getMessage());
        }
    }

    public void log(String category, String action, String actor, String details) {
        String timestamp = DateTimeUtil.formatDateTime(LocalDateTime.now());
        String logLine = String.format("[%s] [%s] [%s] by [%s] - %s",
                timestamp,
                category.toUpperCase(),
                action.toUpperCase(),
                (actor != null && !actor.isEmpty()) ? actor : "SYSTEM",
                details != null ? details : "");

        synchronized (lock) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(logFilePath, true), StandardCharsets.UTF_8))) {
                writer.write(logLine);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("[WARN] Failed to write audit log: " + e.getMessage());
            }
        }
    }

    public List<String> getRecentLogs(int limit) {
        List<String> all = new ArrayList<>();
        File file = new File(logFilePath);
        if (!file.exists()) return all;

        synchronized (lock) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#") && !line.trim().isEmpty()) {
                        all.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("[WARN] Error reading audit logs: " + e.getMessage());
            }
        }

        int start = Math.max(0, all.size() - limit);
        return new ArrayList<>(all.subList(start, all.size()));
    }
}
