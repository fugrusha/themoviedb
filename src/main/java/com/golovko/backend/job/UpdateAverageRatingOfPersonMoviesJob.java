package com.golovko.backend.job;

import com.golovko.backend.repository.PersonRepository;
import com.golovko.backend.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdateAverageRatingOfPersonMoviesJob {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonService personService;

    @Transactional(readOnly = true)
    @Scheduled(cron = "${update.average.rating.of.person.movies.job.cron}")
    public void updateAverageRating() {
        log.info("Job updating average rating of person movies started...");

        personRepository.getIdsOfPeople().forEach(personId -> {
            try {
                personService.updateAverageRatingOfPersonMovies(personId);
            } catch (Exception e) {
                log.error("Failed to update average rating of person movies for person: {}", personId, e);
            }
        });

        log.info("Job finished!");
    }
}
