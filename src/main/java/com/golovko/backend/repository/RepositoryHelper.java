package com.golovko.backend.repository;

import com.golovko.backend.exception.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.UUID;

@Component
public class RepositoryHelper {

    @PersistenceContext
    private EntityManager entityManager;

    public <E> E getReferenceIfExist(Class<E> entityClass, UUID id) {
        validateExists(entityClass, id);
        return entityManager.getReference(entityClass, id);
    }

    public <E> E getEntityById(Class<E> entityClass, UUID id) {
        E obj = entityManager.find(entityClass, id);

        if (obj == null) {
            throw new EntityNotFoundException(entityClass, id);
        }
        return obj;
    }

    public <E> void validateExists(Class<E> entityClass, UUID id) {
        Query query = entityManager.createQuery(
                "select count(e) from " + entityClass.getSimpleName() + " e where e.id = :id");

        query.setParameter("id", id);
        boolean exists = ((Number) query.getSingleResult()).intValue() > 0;
        if (!exists) {
            throw new EntityNotFoundException(entityClass, id);
        }
    }

    public void applyPaging(Query query, Pageable pageable) {
        if (pageable.isPaged()) {
            query.setMaxResults(pageable.getPageSize());
            query.setFirstResult((int) pageable.getOffset());
        }
    }
}
