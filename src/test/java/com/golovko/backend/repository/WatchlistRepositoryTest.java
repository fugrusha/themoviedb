package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Watchlist;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

public class WatchlistRepositoryTest extends BaseTest {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        Instant createdAtBeforeReload = watchlist.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        watchlist = watchlistRepository.findById(watchlist.getId()).get();

        Instant createdAtAfterReload = watchlist.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        Instant modifiedAtBeforeReload = watchlist.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        watchlist = watchlistRepository.findById(watchlist.getId()).get();
        watchlist.setName("Another Name");
        watchlist = watchlistRepository.save(watchlist);
        Instant modifiedAtAfterReload = watchlist.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testFindByAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Watchlist w1 = testObjectFactory.createWatchlist(user1, null);
        Watchlist w2 = testObjectFactory.createWatchlist(user1, null);
        testObjectFactory.createWatchlist(user2, null);
        testObjectFactory.createWatchlist(user2, null);

        List<Watchlist> watchlists = watchlistRepository.findByAuthorId(user1.getId());

        Assertions.assertThat(watchlists).extracting("id")
                .containsExactlyInAnyOrder(w1.getId(), w2.getId());
    }

    @Test
    public void testFindByIdAndAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Watchlist w1 = testObjectFactory.createWatchlist(user1, null);
        testObjectFactory.createWatchlist(user1, null);
        testObjectFactory.createWatchlist(user2, null);

        Watchlist watchlist = watchlistRepository.findByIdAndAuthorId(w1.getId(), user1.getId());

        Assert.assertEquals(w1.getId(), watchlist.getId());
    }
}
