package com.hms.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) interface defining CRUD contracts.
 * Demonstrates Generics and Abstraction.
 *
 * @param <T> Entity type implementing Identifiable
 */
public interface Repository<T extends Identifiable> {

    Optional<T> findById(String id);

    List<T> findAll();

    T save(T entity);

    boolean deleteById(String id);

    boolean existsById(String id);

    int count();

    List<T> search(String query);

    void reload();
}
