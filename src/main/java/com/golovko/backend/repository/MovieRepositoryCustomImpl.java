package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class MovieRepositoryCustomImpl implements MovieRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Movie> findByFilter(MovieFilter filter, Pageable pageable) {
        StringBuilder sb = new StringBuilder();
        sb.append("select m from Movie m");

        Query query = createQueryApplyingFilter(filter, pageable.getSort(), sb);
        applyPaging(query, pageable);

        List<Movie> data = query.getResultList();

        long count = getCountOfMovies(filter);
        return new PageImpl<>(data, pageable, count);
    }

    private Query createQueryApplyingFilter(MovieFilter filter, Sort sort, StringBuilder sb) {
        if (filter.getGenreNames() != null
                || filter.getMovieCrewTypes() != null
                || filter.getPersonId() != null) {
            if (filter.getGenreNames() != null && !filter.getGenreNames().isEmpty()) {
                sb.append(" join m.genres g");
            }
            if (filter.getMovieCrewTypes() != null && !filter.getMovieCrewTypes().isEmpty()
                    || filter.getPersonId() != null) {
                sb.append(" join m.movieCrews mcr");
            }
        }

        sb.append(" where 1=1");

        if (filter.getGenreNames() != null && !filter.getGenreNames().isEmpty()) {
            sb.append(" and g.genreName in (:genreNames)");
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

        if (sort != null && sort.isSorted()) {
            sb.append(" order by");
            for (Sort.Order order : sort.toList()) {
                sb.append(" m.").append(order.getProperty()).append(" ").append(order.getDirection());
            }
        }

        Query query = entityManager.createQuery(sb.toString());

        if (filter.getPersonId() != null) {
            query.setParameter("personId", filter.getPersonId());
        }
        if (filter.getMovieCrewTypes() != null && !filter.getMovieCrewTypes().isEmpty()) {
            query.setParameter("movieCrewTypes", filter.getMovieCrewTypes());
        }
        if (filter.getGenreNames() != null && !filter.getGenreNames().isEmpty()) {
            query.setParameter("genreNames", filter.getGenreNames());
        }
        if (filter.getReleasedFrom() != null) {
            query.setParameter("releasedFrom", filter.getReleasedFrom());
        }
        if (filter.getReleasedTo() != null) {
            query.setParameter("releasedTo", filter.getReleasedTo());
        }

        return query;
    }

    private void applyPaging(Query query, Pageable pageable) {
        if (pageable.isPaged()) {
            query.setMaxResults(pageable.getPageSize());
            query.setFirstResult((int) pageable.getOffset());
        }
    }

    private long getCountOfMovies(MovieFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(m) from Movie m");
        Query query = createQueryApplyingFilter(filter, null, sb);
        return ((Number) query.getResultList().get(0)).longValue();
    }
}
