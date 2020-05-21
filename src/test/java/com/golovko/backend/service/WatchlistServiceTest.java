package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Watchlist;
import com.golovko.backend.dto.watchlist.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.WatchlistRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class WatchlistServiceTest extends BaseTest {

    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void testGetUserWatchlist() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        WatchlistReadDTO readDTO = watchlistService.getUserWatchlist(user.getId(), watchlist.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(watchlist, "authorId");
        Assert.assertEquals(user.getId(), readDTO.getAuthorId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetUserWatchlistWrongId() {
        ApplicationUser user = testObjectFactory.createUser();

        watchlistService.getUserWatchlist(user.getId(), UUID.randomUUID());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetUserWatchlistWrongUserId() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        watchlistService.getUserWatchlist(UUID.randomUUID(), watchlist.getId());
    }

    @Test
    public void testGetUserWatchlistExtended() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, List.of(movie));

        WatchlistReadExtendedDTO readDTO = watchlistService.getUserWatchlistExtended(user.getId(), watchlist.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(watchlist, "movies", "author");
        Assertions.assertThat(readDTO.getMovies()).extracting("id").contains(movie.getId());
        Assert.assertEquals(user.getId(), readDTO.getAuthor().getId());
    }

    @Test
    public void testCreateWatchlist() {
        ApplicationUser user = testObjectFactory.createUser();

        WatchlistCreateDTO createDTO = new WatchlistCreateDTO();
        createDTO.setName("watch later");

        WatchlistReadDTO readDTO = watchlistService.createWatchlist(user.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Watchlist watchlist = watchlistRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(watchlist).isEqualToIgnoringGivenFields(readDTO, "movies", "author");
        Assert.assertEquals(user.getId(), readDTO.getAuthorId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateWatchlistWrongUserId() {
        WatchlistCreateDTO createDTO = new WatchlistCreateDTO();
        createDTO.setName("watch later");

        watchlistService.createWatchlist(UUID.randomUUID(), createDTO);
    }

    @Test
    public void testUpdateWatchlist() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        WatchlistPutDTO putDTO = new WatchlistPutDTO();
        putDTO.setName("new name");

        WatchlistReadDTO readDTO = watchlistService.updateWatchlist(user.getId(), watchlist.getId(), putDTO);

        Assertions.assertThat(putDTO).isEqualToComparingFieldByField(readDTO);

        watchlist = watchlistRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(watchlist).isEqualToIgnoringGivenFields(readDTO, "author", "movies");
        Assert.assertEquals(watchlist.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchWatchlist() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        WatchlistPatchDTO patchDTO = new WatchlistPatchDTO();
        patchDTO.setName("new name");

        WatchlistReadDTO readDTO = watchlistService.patchWatchlist(user.getId(), watchlist.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        watchlist = watchlistRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(watchlist).isEqualToIgnoringGivenFields(readDTO, "author", "movies");
        Assert.assertEquals(watchlist.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testDeleteWatchlist() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        watchlistService.deleteWatchlist(user.getId(), watchlist.getId());

        Assert.assertFalse(watchlistRepository.existsById(watchlist.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteWatchlistUserNotFound() {
        ApplicationUser user = testObjectFactory.createUser();
        Watchlist watchlist = testObjectFactory.createWatchlist(user, null);

        watchlistService.deleteWatchlist(UUID.randomUUID(), watchlist.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteWatchlistWatchlistNotFound() {
        ApplicationUser user = testObjectFactory.createUser();

        watchlistService.deleteWatchlist(user.getId(), UUID.randomUUID());
    }
}
