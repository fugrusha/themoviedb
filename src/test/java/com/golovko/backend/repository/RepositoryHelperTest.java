package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.exception.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class RepositoryHelperTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private RepositoryHelper repoHelper;

    @Test
    public void testGetReferenceIfExists() {
        ApplicationUser user = testObjectFactory.createUser();

        ApplicationUser userEntity = applicationUserRepository.findById(user.getId()).get();

        ApplicationUser userReference = repoHelper.getReferenceIfExist(ApplicationUser.class, user.getId());

        Assertions.assertThat(userReference).isInstanceOf(HibernateProxy.class);
        Assertions.assertThat(userEntity).isInstanceOf(ApplicationUser.class);
        Assert.assertEquals(userReference.getId(), userEntity.getId());
        Assertions.assertThat(userEntity).isNotEqualTo(userReference);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetReferenceByWrongId() {
        repoHelper.getReferenceIfExist(ApplicationUser.class, UUID.randomUUID());
    }

    @Test
    public void testGetEntityById() {
        ApplicationUser user = testObjectFactory.createUser();

        ApplicationUser userFromDB = applicationUserRepository.findById(user.getId()).get();

        ApplicationUser userEntity = repoHelper.getEntityById(ApplicationUser.class, user.getId());

        Assertions.assertThat(userEntity).isInstanceOf(ApplicationUser.class);
        Assert.assertEquals(userFromDB.getId(), userEntity.getId());
        Assertions.assertThat(userEntity).isEqualToIgnoringGivenFields(userFromDB,
                "articles", "likes", "userRoles", "comments", "topMatches");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetEntityByWrongId() {
        repoHelper.getEntityById(ApplicationUser.class, UUID.randomUUID());
    }
}
