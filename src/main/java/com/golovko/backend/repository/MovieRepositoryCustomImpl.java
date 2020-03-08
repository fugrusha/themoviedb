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
        sb.append("select m from Movie m");

        if (filter.getGenreIds() != null
                || filter.getMovieCrewTypes() != null
                || filter.getPersonId() != null) {
            if (filter.getGenreIds() != null && !filter.getGenreIds().isEmpty()) {
                sb.append(" join m.genres g");
            }
            if (filter.getMovieCrewTypes() != null && !filter.getMovieCrewTypes().isEmpty()
                    || filter.getPersonId() != null) {
                sb.append(" join m.movieCrews mcr");
            }
        }

        sb.append(" where 1=1");

        if (filter.getGenreIds() != null && !filter.getGenreIds().isEmpty()) {
            sb.append(" and g.id in (:genreIds)");
        }
        if (filter.getMovieCrewTypes() != null && !filter.getMovieCrewTypes().isEmpty()) {
            sb.append(" and mcr.movieCrewType in (:movieCrewTypes)");
        }
        if (filter.getPersonId() != null) {
            sb.append(" and mcr.person.id = :personId");
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
        if (filter.getGenreIds() != null && !filter.getGenreIds().isEmpty()) {
            query.setParameter("genreIds", filter.getGenreIds());
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
