package com.golovko.backend.security;

import com.golovko.backend.repository.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserValidator {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private AuthenticationResolver authenticationResolver;

    public boolean isCurrentUser(UUID userId) {
        Authentication authentication = authenticationResolver.getCurrentAuthentication();
        return applicationUserRepository.existsByIdAndEmail(userId, authentication.getName());
    }
}
