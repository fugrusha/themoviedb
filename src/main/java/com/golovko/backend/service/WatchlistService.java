package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Watchlist;
import com.golovko.backend.dto.watchlist.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
import com.golovko.backend.repository.RepositoryHelper;
import com.golovko.backend.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private TranslationService translationService;

    @Transactional(readOnly = true)
    public List<WatchlistReadDTO> getAllUserWatchlists(UUID userId) {
        List<Watchlist> watchlists = watchlistRepository.findByAuthorId(userId);
        return translationService.translateList(watchlists, WatchlistReadDTO.class);
    }

    @Transactional(readOnly = true)
    public WatchlistReadDTO getUserWatchlist(UUID userId, UUID watchlistId) {
        Watchlist watchlist = getWatchlistRequired(userId, watchlistId);
        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    @Transactional(readOnly = true)
    public WatchlistReadExtendedDTO getUserWatchlistExtended(UUID userId, UUID watchlistId) {
        Watchlist watchlist = getWatchlistRequired(userId, watchlistId);
        return translationService.translate(watchlist, WatchlistReadExtendedDTO.class);
    }

    public WatchlistReadDTO createWatchlist(UUID userId, WatchlistCreateDTO createDTO) {
        ApplicationUser user = repoHelper.getReferenceIfExist(ApplicationUser.class, userId);

        Watchlist watchlist = translationService.translate(createDTO, Watchlist.class);
        watchlist.setAuthor(user);
        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    public WatchlistReadDTO updateWatchlist(UUID userId, UUID watchlistId, WatchlistPutDTO putDTO) {
        Watchlist watchlist = getWatchlistRequired(userId, watchlistId);

        translationService.map(putDTO, watchlist);
        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    public WatchlistReadDTO patchWatchlist(UUID userId, UUID watchlistId, WatchlistPatchDTO patchDTO) {
        Watchlist watchlist = getWatchlistRequired(userId, watchlistId);

        translationService.map(patchDTO, watchlist);
        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    public void deleteWatchlist(UUID userId, UUID watchlistId) {
        Watchlist watchlist = getWatchlistRequired(userId, watchlistId);
        watchlistRepository.delete(watchlist);
    }

    @Transactional
    public WatchlistReadDTO addMovieToWatchlist(UUID userId, UUID watchlistId, UUID movieId) {
        Watchlist watchlist = getWatchlistRequired(userId, watchlistId);
        Movie movie = repoHelper.getReferenceIfExist(Movie.class, movieId);

        if (watchlist.getMovies().stream().anyMatch(m -> m.getId().equals(movieId))) {
            throw new LinkDuplicatedException(String.format("Watchlist %s already has movie %s",
                    watchlistId, movieId));
        }

        watchlist.getMovies().add(movie);
        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    @Transactional
    public WatchlistReadDTO removeMovieFromWatchlist(UUID userId, UUID watchlistId, UUID movieId) {
        Watchlist watchlist = repoHelper.getEntityById(Watchlist.class, watchlistId);

        boolean removed = watchlist.getMovies().removeIf(m -> m.getId().equals(movieId));

        if (!removed) {
            throw new EntityNotFoundException("Watchlist " + watchlistId + " has no movie " + movieId);
        }

        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);

    }

    private Watchlist getWatchlistRequired(UUID userId, UUID watchlistId) {
        return Optional.ofNullable(watchlistRepository.findByIdAndAuthorId(watchlistId, userId))
                .orElseThrow(() -> new EntityNotFoundException(Watchlist.class, watchlistId, userId));
    }
}
