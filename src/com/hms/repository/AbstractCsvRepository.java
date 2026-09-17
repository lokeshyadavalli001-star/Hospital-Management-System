package com.hms.repository;

import com.hms.model.Searchable;
import com.hms.util.FileUtil;
import com.hms.util.IdGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reusable abstract CSV persistence layer.
 * Demonstrates Template Method design pattern, Generics, Collections, and robust File I/O.
 *
 * @param <T> Domain entity implementing Identifiable
 */
public abstract class AbstractCsvRepository<T extends Identifiable> implements Repository<T> {

    protected final String filePath;
    protected final Map<String, T> cache = new LinkedHashMap<>();
    private final Object lock = new Object();

    public AbstractCsvRepository(String filePath) {
        this.filePath = filePath;
        initFile();
        loadAll();
    }

    protected abstract String getCsvHeader();

    protected abstract String serialize(T entity);

    protected abstract T deserialize(List<String> tokens) throws Exception;

    private void initFile() {
        try {
            FileUtil.ensureFileExists(filePath, getCsvHeader());
        } catch (IOException e) {
            System.err.println("[WARN] Could not initialize storage file: " + filePath + " - " + e.getMessage());
        }
    }

    @Override
    public void reload() {
        loadAll();
    }

    private void loadAll() {
        synchronized (lock) {
            cache.clear();
            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) {
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line = reader.readLine(); // Skip header
                int lineNum = 1;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        List<String> tokens = FileUtil.parseCsvLine(line);
                        T entity = deserialize(tokens);
                        if (entity != null) {
                            cache.put(entity.getId(), entity);
                            IdGenerator.observeExistingId(entity.getId());
                        }
                    } catch (Exception ex) {
                        System.err.println("[WARN] Skipping malformed record in " + filePath + " at line " + lineNum + ": " + ex.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("[ERROR] Failed reading storage file: " + filePath + " - " + e.getMessage());
            }
        }
    }

    protected void persistAll() {
        synchronized (lock) {
            File file = new File(filePath);
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
                writer.write(getCsvHeader());
                writer.newLine();
                for (T entity : cache.values()) {
                    writer.write(serialize(entity));
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("[ERROR] Failed persisting records to " + filePath + " - " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<T> findById(String id) {
        if (id == null) return Optional.empty();
        synchronized (lock) {
            return Optional.ofNullable(cache.get(id.trim().toUpperCase()));
        }
    }

    @Override
    public List<T> findAll() {
        synchronized (lock) {
            return new ArrayList<>(cache.values());
        }
    }

    @Override
    public T save(T entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Entity or entity ID cannot be null");
        }
        synchronized (lock) {
            cache.put(entity.getId().trim().toUpperCase(), entity);
            persistAll();
            return entity;
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) return false;
        synchronized (lock) {
            T removed = cache.remove(id.trim().toUpperCase());
            if (removed != null) {
                persistAll();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        synchronized (lock) {
            return cache.containsKey(id.trim().toUpperCase());
        }
    }

    @Override
    public int count() {
        synchronized (lock) {
            return cache.size();
        }
    }

    @Override
    public List<T> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        String clean = query.trim().toLowerCase();
        List<T> results = new ArrayList<>();
        synchronized (lock) {
            for (T entity : cache.values()) {
                if (entity instanceof Searchable) {
                    if (((Searchable) entity).matches(clean)) {
                        results.add(entity);
                    }
                } else if (entity.getId().toLowerCase().contains(clean)) {
                    results.add(entity);
                }
            }
        }
        return results;
    }
}
