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
        sb.append("select m from Movie m join fetch m.movieCrews mc where 1=1");
        if (filter.getPersonId() != null) {
            sb.append(" and mc.person.id = :personId");
        }
        if (filter.getMovieCrewTypes() != null && !filter.getMovieCrewTypes().isEmpty()) {
            sb.append(" and mc.movieCrewType in (:movieCrewTypes)");
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
        if (filter.getMovieCrewTypes() != null && !filter.getMovieCrewTypes().isEmpty()) {
            query.setParameter("movieCrewTypes", filter.getMovieCrewTypes());
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
