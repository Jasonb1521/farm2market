package com.farm2market.core.repository;

import com.farm2market.core.model.Entity;
import java.util.List;
import java.util.Optional;

public interface Repository<T extends Entity<ID>, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findAll(int limit, int offset);

    T save(T entity);

    T update(T entity);

    void delete(ID id);

    boolean existsById(ID id);

    long count();
}
