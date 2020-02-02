package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

public class MovieRepositoryCustomImpl implements MovieRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Movie> findByFilter(MovieFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select m from Movie m join fetch m.movieParticipations mp where 1=1");
        if (filter.getPersonId() != null) {
            sb.append(" and mp.person.id = :personId");
        }
        if (filter.getPartTypes() != null && !filter.getPartTypes().isEmpty()) {
            sb.append(" and mp.partType in (:partTypes)");
        }
        if (filter.getReleasedFrom() != null) {
            sb.append(" and m.releaseDate >= (:releasedFrom)");
        }
        if (filter.getReleasedTo() != null) {
            sb.append(" and m.releaseDate < (:releasedTo)");
        }

        TypedQuery<Movie> query = entityManager.createQuery(sb.toString(), Movie.class);

        if (filter.getPersonId() != null) {
            query.setParameter("personId", filter.getPersonId());
        }
        if (filter.getPartTypes() != null && !filter.getPartTypes().isEmpty()) {
            query.setParameter("partTypes", filter.getPartTypes());
        }
        if (filter.getReleasedFrom() != null) {
            query.setParameter("releasedFrom", filter.getReleasedFrom());
        }
        if (filter.getReleasedTo() != null) {
            query.setParameter("releasedTo", filter.getReleasedTo());
        }

        return query.getResultList();
    }
}
