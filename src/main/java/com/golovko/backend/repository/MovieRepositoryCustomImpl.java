package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieFilter;
import org.apache.commons.collections.CollectionUtils;
import org.bitbucket.brunneng.qb.JpaQueryBuilder;
import org.bitbucket.brunneng.qb.SpringQueryBuilderUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class MovieRepositoryCustomImpl implements MovieRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Movie> findByFilter(MovieFilter filter, Pageable pageable) {
        JpaQueryBuilder qb = new JpaQueryBuilder(entityManager);
        qb.append("select m from Movie m");
        qb.appendJoin("join m.genres g");

        if (CollectionUtils.isNotEmpty(filter.getMovieCrewTypes())
                || filter.getPersonId() != null) {
            qb.append("join m.movieCrews mcr");
        }

        qb.append("where 1=1");
        qb.appendIn("and g.genreName in :v", filter.getGenreNames());
        qb.appendIn("and mcr.movieCrewType in :v", filter.getMovieCrewTypes());
        qb.append("and mcr.person.id = :v", filter.getPersonId());
        qb.append("and m.releaseDate >= :v", filter.getReleasedFrom());
        qb.append("and m.releaseDate < :v", filter.getReleasedTo());

        return SpringQueryBuilderUtils.loadPage(qb, pageable, "id");
    }
}
