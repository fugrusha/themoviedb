package com.golovko.backend.repository;

import com.golovko.backend.domain.Watchlist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends CrudRepository<Watchlist, UUID> {

    List<Watchlist> findByAuthorId(UUID userId);

    Watchlist findByIdAndAuthorId(UUID userId, UUID watchlistId);
}
