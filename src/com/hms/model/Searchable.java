package com.hms.model;

/**
 * Contract for entities that support keyword/text filtering.
 */
public interface Searchable {
    /**
     * Checks whether this entity satisfies the search query.
     *
     * @param query case-insensitive search string
     * @return true if entity matches
     */
    boolean matches(String query);
}
