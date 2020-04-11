package com.golovko.backend.job;

import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdatePredictedAverageRatingOfMoviesJob {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    @Transactional(readOnly = true)
    @Scheduled(cron = "${update.predicted.average.rating.of.movies.job.cron}")
    public void updatePredictedAverageRating() {
        log.info("Job started...");

        movieRepository.getIdsOfUnreleasedMovies().forEach(movieId -> {
            try {
                movieService.updatePredictedAverageRatingOfMovie(movieId);
            } catch (Exception e) {
                log.error("Failed to update predicted average rating for movie: {}", movieId, e);
            }
        });

        log.info("Job finished!");
    }
}
