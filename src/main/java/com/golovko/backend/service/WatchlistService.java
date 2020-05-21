package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Watchlist;
import com.golovko.backend.dto.watchlist.*;
import com.golovko.backend.exception.EntityNotFoundException;
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
    public WatchlistReadDTO getUserWatchlist(UUID userId, UUID id) {
        Watchlist watchlist = getWatchlistRequired(userId, id);
        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    @Transactional(readOnly = true)
    public WatchlistReadExtendedDTO getUserWatchlistExtended(UUID userId, UUID id) {
        Watchlist watchlist = getWatchlistRequired(userId, id);
        return translationService.translate(watchlist, WatchlistReadExtendedDTO.class);
    }

    public WatchlistReadDTO createWatchlist(UUID userId, WatchlistCreateDTO createDTO) {
        ApplicationUser user = repoHelper.getReferenceIfExist(ApplicationUser.class, userId);

        Watchlist watchlist = translationService.translate(createDTO, Watchlist.class);
        watchlist.setAuthor(user);
        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    public WatchlistReadDTO updateWatchlist(UUID userId, UUID id, WatchlistPutDTO putDTO) {
        Watchlist watchlist = getWatchlistRequired(userId, id);

        translationService.map(putDTO, watchlist);
        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    public WatchlistReadDTO patchWatchlist(UUID userId, UUID id, WatchlistPatchDTO patchDTO) {
        Watchlist watchlist = getWatchlistRequired(userId, id);

        translationService.map(patchDTO, watchlist);
        watchlist = watchlistRepository.save(watchlist);

        return translationService.translate(watchlist, WatchlistReadDTO.class);
    }

    public void deleteWatchlist(UUID userId, UUID id) {
        Watchlist watchlist = getWatchlistRequired(userId, id);
        watchlistRepository.delete(watchlist);
    }

    private Watchlist getWatchlistRequired(UUID userId, UUID watchlistId) {
        return Optional.ofNullable(watchlistRepository.findByIdAndAuthorId(watchlistId, userId))
                .orElseThrow(() -> new EntityNotFoundException(Watchlist.class, watchlistId, userId));
    }
}
