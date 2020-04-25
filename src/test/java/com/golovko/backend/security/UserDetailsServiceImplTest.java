package com.golovko.backend.security;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.UserRoleRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.IterableUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

public class UserDetailsServiceImplTest extends BaseTest {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    public void testLoadUserByUsername() {
        ApplicationUser user = testObjectFactory.createUser();
        user.getUserRoles().addAll(new ArrayList<>(IterableUtil.toCollection(userRoleRepository.findAll())));
        user = applicationUserRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        Assert.assertEquals(user.getEncodedPassword(), userDetails.getPassword());
        Assert.assertEquals(user.getEmail(), userDetails.getUsername());
        Assert.assertNotNull(userDetails.getAuthorities());

        Assertions.assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(user.getUserRoles().stream()
                        .map(role -> role.getType().toString())
                        .toArray());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() {
        userDetailsService.loadUserByUsername("wrong email");
    }
}
