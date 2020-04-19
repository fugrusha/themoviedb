package com.golovko.backend.job.oneoff;

import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.MovieReadShortDTO;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.service.AsyncService;
import com.golovko.backend.service.importer.MovieImporterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class TheMovieDbImportOneOffJob {

    @Autowired
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private MovieImporterService movieImporterService;

    @Autowired
    private AsyncService asyncService;

    @Value("${themoviedb.import.job.enabled}")
    private boolean enabled;

    @PostConstruct
    void executeJob() {
        if (!enabled) {
            log.info("Import is disabled");
            return;
        }

        asyncService.executeAsync(this::doImport);
    }

    public void doImport() {
        log.info("Starting import ...");

        try {
            List<MovieReadShortDTO> moviesToImport = theMovieDbClient.getTopRatedMovies().getResults();
            int successfullyImported = 0;
            int failed = 0;
            int skipped = 0;

            for (MovieReadShortDTO dto : moviesToImport) {
                try {
                    movieImporterService.importMovie(dto.getId());
                    successfullyImported++;
                } catch (ImportedEntityAlreadyExistsException | ImportAlreadyPerformedException ex) {
                    log.info("Can't import movie id={}, title={}: {}", dto.getId(), dto.getTitle(), ex.getMessage());
                    skipped++;
                } catch (Exception ex) {
                    log.warn("Failed to import movie id={}. title={} {}", dto.getId(), dto.getTitle(), ex.getMessage());
                    failed++;
                }
            }

            log.info("Total movies to import: {}, successfully imported: {}, skipped: {}, failed: {}",
                    moviesToImport.size(), successfullyImported, skipped, failed);
        } catch (Exception ex) {
            log.warn("Failed to perform import", ex);
        }

        log.info("Import finished");
    }
}
