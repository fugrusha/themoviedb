package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from user_role", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RepositoryHelperTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

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
                "articles", "likes");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetEntityByWrongId() {
        repoHelper.getEntityById(ApplicationUser.class, UUID.randomUUID());
    }
}
