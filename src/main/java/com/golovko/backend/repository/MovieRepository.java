package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieInLeaderBoardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MovieRepository extends CrudRepository<Movie, UUID>, MovieRepositoryCustom {

    @Query("select m.id from Movie m")
    Stream<UUID> getIdsOfMovies();

    @Query("select avg(m.averageRating) from Movie m"
            + " join m.movieCasts mc"
            + " where mc.person.id = :personId")
    Double calcAverageRatingOfPersonMovies(UUID personId);

    @Modifying
    @Query("update Movie m set m.likesCount=(m.likesCount + 1)"
            + " where m.id = :movieId")
    void incrementLikesCountField(UUID movieId);

    @Modifying
    @Query("update Movie m set m.likesCount=(m.likesCount - 1)"
            + " where m.id = :movieId")
    void decrementLikesCountField(UUID movieId);

    @Modifying
    @Query("update Movie m set m.dislikesCount=(m.dislikesCount + 1)"
            + " where m.id = :movieId")
    void incrementDislikesCountField(UUID movieId);

    @Modifying
    @Query("update Movie m set m.dislikesCount=(m.dislikesCount - 1)"
            + " where m.id = :movieId")
    void decrementDislikesCountField(UUID movieId);

    @Query("select m.id from Movie m where m.isReleased = false")
    Stream<UUID> getIdsOfUnreleasedMovies();

    @Query("select new com.golovko.backend.dto.movie.MovieInLeaderBoardDTO(m.id,"
            + " m.movieTitle, m.averageRating, m.likesCount, m.dislikesCount)"
            + " from Movie m")
    Page<MovieInLeaderBoardDTO> getMoviesLeaderBoard(Pageable pageable);
}
