package com.golovko.backend.job;

import com.golovko.backend.repository.MovieCastRepository;
import com.golovko.backend.service.MovieCastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdateAverageRatingOfMovieCastsJob {

    @Autowired
    private MovieCastService movieCastService;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Transactional(readOnly = true)
    @Scheduled(cron = "${update.average.mark.of.movieCasts.job.cron}")
    public void updateAverageRatingOfMovieCast() {
        log.info("Job started...");

        movieCastRepository.getIdsOfMovieCasts().forEach(movieCastId -> {
            try {
                movieCastService.updateAverageRatingOfMovieCast(movieCastId);
            } catch (Exception e) {
                log.error("Failed to update average rating for movieCast: {}", movieCastId, e);
            }
        });

        log.info("Job finished!");
    }
}
