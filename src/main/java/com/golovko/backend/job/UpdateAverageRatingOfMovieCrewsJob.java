package com.golovko.backend.job;

import com.golovko.backend.repository.MovieCrewRepository;
import com.golovko.backend.service.MovieCrewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdateAverageRatingOfMovieCrewsJob {

    @Autowired
    private MovieCrewService movieCrewService;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Transactional(readOnly = true)
    @Scheduled(cron = "${update.average.rating.of.movieCrews.job.cron}")
    public void updateAverageRatingOfMovieCrews() {
        log.info("Job started...");

        movieCrewRepository.getIdsOfMovieCrews().forEach(movieCrewId -> {
            try {
                movieCrewService.updateAverageRatingOfMovieCrew(movieCrewId);
            } catch (Exception e) {
                log.error("Failed to update average rating for movieCrew: {}", movieCrewId, e);
            }
        });

        log.info("Job finished!");
    }
}
