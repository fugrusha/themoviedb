package com.golovko.backend.security;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRoleType;
import com.golovko.backend.repository.ApplicationUserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

public class CurrentUserValidatorTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private CurrentUserValidator currentUserValidator;

    @MockBean
    private AuthenticationResolver authenticationResolver;

    @Test
    public void testIsCurrentUser() {
        ApplicationUser user = testObjectFactory.createUser();

        Authentication authentication = new TestingAuthenticationToken(user.getEmail(), null);
        Mockito.when(authenticationResolver.getCurrentAuthentication()).thenReturn(authentication);

        Assert.assertTrue(currentUserValidator.isCurrentUser(user.getId()));
    }

    @Test
    public void testIsDifferentUser() {
        ApplicationUser user1 = testObjectFactory.createUser("u1@mail.com", "123", UserRoleType.USER);
        ApplicationUser user2 = testObjectFactory.createUser("u2@mail.com", "123", UserRoleType.USER);

        Authentication authentication = new TestingAuthenticationToken(user1.getEmail(), null);
        Mockito.when(authenticationResolver.getCurrentAuthentication()).thenReturn(authentication);

        Assert.assertFalse(currentUserValidator.isCurrentUser(user2.getId()));
    }
}